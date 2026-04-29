# Migrating to User/Password Authentication

## Current Approach

The app currently uses the **Supabase anonymous key** (`SUPABASE_ANON_KEY`) for all remote access — no user identity is involved. Data isolation is enforced at the application layer: every row carries a `device_id` column populated from `ANDROID_ID` (`DeviceIdProvider`), and every remote query filters on `eq("device_id", deviceIdProvider.id)`. The Supabase RLS policies are wide-open to the anon role:

```sql
CREATE POLICY "anon_all_stores"   ON stores   FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "anon_all_products" ON products FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "anon_all_sales"    ON sales    FOR ALL TO anon USING (true) WITH CHECK (true);
```

This means any caller with the anon key can read or write any row in the database. The `device_id` filter only runs in Kotlin — if the key leaks, all data is exposed.

The `SupabaseClient` is constructed in `di/SupabaseModule.kt` with only the `Postgrest` and `Storage` plugins; there is no `Auth` plugin.

---

## Target Approach

Replace anon-key access with **Supabase Auth email/password**. Each user signs up and logs in, receiving a JWT scoped to their `auth.uid()`. RLS policies enforce data ownership server-side:

```sql
-- Only the row's owner can see or modify it
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id)
```

The `device_id` column can optionally be kept for auditing (which device created a row) but is no longer used for data isolation.

---

## Migration Steps

### 1. Enable Supabase Auth (Dashboard)

1. Open **Authentication → Providers → Email** and ensure it is enabled.
2. Configure **Site URL** and **Redirect URLs** (for deep links if you add email confirmation later).
3. Optionally disable email confirmation during development: **Authentication → Email Templates → Confirm signup → Disable**.

---

### 2. Add a `user_id` column to `stores` only

`products` and `sales` already express ownership through their `store_id` FK — no redundant column needed.

```sql
ALTER TABLE stores ADD COLUMN user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE;
```

Leave `device_id` in place for now — it will be used during the data migration step below.

---

### 3. Replace RLS policies

Drop the open anon policies and create auth-scoped ones:

```sql
-- Stores: direct ownership check
DROP POLICY "anon_all_stores" ON stores;
CREATE POLICY "owner_stores" ON stores
    FOR ALL TO authenticated
    USING  (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Products: inherit ownership through store_id → stores.user_id
DROP POLICY "anon_all_products" ON products;
CREATE POLICY "owner_products" ON products
    FOR ALL TO authenticated
    USING  (EXISTS (SELECT 1 FROM stores WHERE stores.id = products.store_id AND stores.user_id = auth.uid()))
    WITH CHECK (EXISTS (SELECT 1 FROM stores WHERE stores.id = products.store_id AND stores.user_id = auth.uid()));

-- Sales: inherit ownership through store_id → stores.user_id
DROP POLICY "anon_all_sales" ON sales;
CREATE POLICY "owner_sales" ON sales
    FOR ALL TO authenticated
    USING  (EXISTS (SELECT 1 FROM stores WHERE stores.id = sales.store_id AND stores.user_id = auth.uid()))
    WITH CHECK (EXISTS (SELECT 1 FROM stores WHERE stores.id = sales.store_id AND stores.user_id = auth.uid()));

-- Storage (scope to the user's own folder)
DROP POLICY "anon_all_storage" ON storage.objects;
CREATE POLICY "owner_storage" ON storage.objects
    FOR ALL TO authenticated
    USING  ((storage.foldername(name))[1] = auth.uid()::text)
    WITH CHECK ((storage.foldername(name))[1] = auth.uid()::text);
```

> After this step, anonymous requests will receive a 401. Do **not** deploy the app changes until steps 4–9 are complete.

---

### 4. Migrate existing rows to the new user (one-time SQL)

If the Supabase project already holds real data tied to a known `device_id`, you can claim those rows for the new user account in the Supabase SQL editor. Run this after the new user account exists:

```sql
-- Replace '<your-new-user-uuid>' with the UUID from auth.users
-- Only stores needs updating — products and sales inherit ownership through store_id
UPDATE stores SET user_id = '<your-new-user-uuid>' WHERE device_id = '<your-device-id>';
```

Find your device's `ANDROID_ID` by logging `DeviceIdProvider.id` once before the migration, or read it from an existing Supabase row.

---

### 5. Update `SupabaseModule.kt` — add the Auth plugin

```kotlin
// di/SupabaseModule.kt
import io.github.jan.supabase.auth.Auth

fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl  = BuildConfig.SUPABASE_URL,
    supabaseKey  = BuildConfig.SUPABASE_ANON_KEY   // anon key is still used pre-login
) {
    install(Postgrest)
    install(Storage)
    install(Auth)                                  // add this
}
```

The Supabase Kotlin SDK automatically attaches the authenticated JWT to every `Postgrest` and `Storage` request once the user is signed in.

---

### 6. Create an `AuthRepository`

```kotlin
// domain/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun signUp(email: String, password: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    fun currentUserId(): String?       // null when not signed in
    fun isSignedIn(): Boolean
}
```

```kotlin
// data/repository/impl/AuthRepositoryImpl.kt
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun signUp(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email    = email
            this.password = password
        }
    }

    override suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email    = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }

    override fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    override fun isSignedIn(): Boolean = supabase.auth.currentUserOrNull() != null
}
```

Bind it in a new Hilt module (or inside `DatabaseModule`):

```kotlin
@Binds abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
```

---

### 7. Update `StoreDto` to carry `user_id`

Only `StoreDto` needs the field — `ProductDto` and `SaleDto` are unchanged because those tables have no `user_id` column.

```kotlin
// data/remote/dto/StoreDto.kt
@Serializable
data class StoreDto(
    // ... existing fields ...
    @SerialName("user_id") val userId: String = ""
)
```

---

### 8. Update the remote data source implementations

Remove the `device_id` filter from all queries — RLS now handles isolation. Only `StoreRemoteDataSourceImpl` needs to set `user_id` on writes; product and sale implementations just drop the `device_id` filter.

```kotlin
// data/remote/source/impl/StoreRemoteDataSourceImpl.kt
class StoreRemoteDataSourceImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository      // replaces DeviceIdProvider
) : StoreRemoteDataSource {

    override suspend fun getAll(): List<StoreDto> =
        supabase.from("stores").select().decodeList()   // RLS filters by auth.uid()

    override suspend fun getById(id: Long): StoreDto? =
        supabase.from("stores")
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()

    override suspend fun insert(dto: StoreDto) {
        val uid = requireNotNull(authRepository.currentUserId())
        supabase.from("stores").upsert(dto.copy(userId = uid))
    }

    override suspend fun update(dto: StoreDto) {
        supabase.from("stores").update(dto) { filter { eq("id", dto.id) } }
    }

    override suspend fun delete(id: Long) {
        supabase.from("stores").delete { filter { eq("id", id) } }
    }
}
```

For `ProductRemoteDataSourceImpl` and `SaleRemoteDataSourceImpl`, only remove the `device_id` filter — no other changes needed, since those tables have no `user_id` column and RLS inherits ownership through `store_id`.

---

### 9. Update `SyncManager` — stamp `user_id` on store DTOs only

`SyncManager.pushAll()` currently stamps `deviceId` on every entity before pushing. Only store DTOs need `userId` now; product and sale DTOs are unchanged.

```kotlin
// Before (all three entity types)
val dtoWithDevice = dto.copy(deviceId = deviceIdProvider.id)

// After — stores only
val uid = requireNotNull(authRepository.currentUserId())
val storeDto = dto.copy(userId = uid)
// product and sale DTOs are pushed as-is; RLS validates them through store_id
```

The `DeviceIdProvider` can be kept if you still want to record which device created a row (for audit purposes), but it no longer drives data isolation.

---

### 10. Update Storage paths to use `user_id`

The current bucket paths use `deviceId` as a folder prefix (e.g. `logos/<deviceId>/<storeId>.jpg`). Switch to `userId` so the storage RLS policy (which checks the first folder segment) grants access correctly:

```kotlin
// data/remote/storage/ImageUploader.kt
// Old:  "logos/${deviceIdProvider.id}/$storeId.jpg"
// New:  "logos/${authRepository.currentUserId()}/$storeId.jpg"
```

---

### 11. Add a Login / Sign-up screen

Add a new feature under `ui/screen/auth/` following the existing screen pattern (Screen + ViewModel + UiState). The ViewModel calls `AuthRepository.signIn()` / `signUp()` and emits a `NavigateToHome` event on success.

In `AppNavigation.kt`:

- On cold start, check `authRepository.isSignedIn()`. If false, navigate to the login route before showing the home screen.
- On sign-out, pop the back stack and navigate back to login.

Add the new routes to `AppRoutes.kt`:

```kotlin
@Serializable object LoginRoute
@Serializable object SignUpRoute
```

---

### 12. Handle token refresh

The Supabase Kotlin SDK refreshes the JWT automatically when the app makes a network call. To persist the session across cold starts, call:

```kotlin
supabase.auth.loadFromStorage()  // call once at app startup, before any remote operations
```

Add this call inside `PhoebeStoreApp.onCreate` before `SyncManager.runInitialSyncIfNeeded()`.

Session storage defaults to in-memory; to persist across process restarts install the `SupabaseComposeAuth` or write a custom `SessionManager` that stores the serialised session in `SharedPreferences` or `DataStore`.

---

## What Does NOT Change

| Component | Impact |
|---|---|
| Room database / DAOs | None — offline-first behaviour is unchanged |
| Domain models | None — `user_id` is an infrastructure detail invisible above the data layer |
| Repository interfaces | `AuthRepository` is new; the others are unchanged |
| `SyncManager` logic (pull / push decision tree) | Logic is the same; only the identity field stamped changes |
| `RemoteErrorHandler` | None |
| All UI screens (except the new auth screen) | None |
| `DeviceIdProvider` | Can be kept for device auditing; no longer used for isolation |

---

## Security Improvements

| Concern | Current (anon key) | After migration |
|---|---|---|
| Unauthenticated access to any row | Possible — `USING (true)` | Blocked — JWT required |
| Key leaked from APK | Exposes all rows to anyone | Leaking anon key still requires valid credentials to read data |
| Cross-user data leakage | Prevented by app-layer `device_id` filter | Prevented server-side by RLS on `auth.uid()` |
| Multi-device access to same account | Not possible | Supported — any device that signs in can access the same data |
