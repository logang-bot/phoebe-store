# Sync & Remote Layer

## Overview

PhoebeStore uses **Supabase** (PostgreSQL via PostgREST) as its remote backend. The app is **offline-first**: Room is always the source of truth for reads and local writes. Supabase receives data asynchronously and serves as a backup / restore point.

---

## Supabase Configuration

Credentials are injected at build time via `BuildConfig`:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://<ref>.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"<anon-jwt>\"")
```

The `SupabaseClient` is provided as a Hilt singleton in `di/SupabaseModule.kt` with the `Postgrest` and `Storage` plugins installed. Ktor OkHttp engine is used for HTTP (reuses the OkHttp instance already present via Coil).

---

## Supabase Schema

Tables mirror the Room schema 1:1. IDs are `bigint primary key` (not `bigserial`) so Room-generated IDs are pushed to Supabase as-is.

```sql
create table stores (
    id bigint primary key,
    name text not null,
    description text not null default '',
    currency text not null default 'USD',
    logo_url text not null default '',
    photo_url text not null default '',
    device_id text not null default '',
    created_at bigint not null
);

create table products (
    id bigint primary key,
    store_id bigint not null references stores(id) on delete cascade,
    name text not null,
    description text not null default '',
    price double precision not null,
    cost_price double precision not null default 0,
    stock int not null default 0,
    image_url text not null default '',
    device_id text not null default '',
    created_at bigint not null
);

create table sales (
    id bigint primary key,
    store_id bigint not null references stores(id) on delete cascade,
    product_id bigint references products(id) on delete set null,
    product_name text not null,
    quantity int not null,
    unit_price double precision not null,
    unit_cost double precision not null default 0,
    total_amount double precision not null,
    sale_type text not null default 'STANDARD',
    profit_outcome text not null default 'NORMAL_PROFIT',
    notes text not null default '',
    on_credit boolean not null default false,
    credit_person_name text not null default '',
    device_id text not null default '',
    sold_at bigint not null,
    created_at bigint not null
);
```

> All timestamps are Unix milliseconds, matching Room's `Long` columns.

> `lastAccessedAt` (Store) is local-only and has no Supabase column.

---

## Device Identification

`data/sync/DeviceIdProvider.kt` is a `@Singleton` that provides a stable device identifier using `Settings.Secure.ANDROID_ID`:

```kotlin
@Singleton
class DeviceIdProvider @Inject constructor(@ApplicationContext private val context: Context) {
    val id: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
```

`ANDROID_ID` is a 64-bit hex string tied to the device + app signing key since Android 8.0. It survives app reinstalls and only changes on factory reset. It is read synchronously via `by lazy` — no coroutines or DataStore involved.

Every row pushed to Supabase includes `device_id = DeviceIdProvider.id`. All remote list queries (`getAll`, `getByStore`) filter by `eq("device_id", deviceIdProvider.id)` so each device only sees its own rows.

---

## Supabase Storage

Images (store logos, cover photos, product images) are uploaded to Supabase Storage and their public HTTPS URLs are stored in both Room and Supabase — replacing local `file://` URIs which are invalid on other devices.

### Buckets

| Bucket | Path pattern | Used for |
|---|---|---|
| `store-images` | `logos/<deviceId>/<storeId>.jpg` | Store logo |
| `store-images` | `photos/<deviceId>/<storeId>.jpg` | Store cover photo |
| `product-images` | `products/<deviceId>/<productId>.jpg` | Product image |

Both buckets are **public** — `publicUrl()` returns a URL that works without auth headers.

### `ImageUploader`

`data/remote/storage/ImageUploader.kt` is a `@Singleton` that wraps all Storage interactions:

```kotlin
suspend fun resolveUrl(localUri: String, bucket: String, remotePath: String): String
```

Behaviour:
- Returns `localUri` unchanged if it is blank or already starts with `https://` — safe to call repeatedly on the same entity.
- Reads the local file as bytes and calls `supabase.storage.from(bucket).upload(remotePath, bytes) { upsert = true }`. `upsert = true` avoids a delete+insert cycle on re-runs.
- Constructs the public URL with `supabase.storage.from(bucket).publicUrl(remotePath)` — no extra network call.
- On failure, falls back to returning `localUri` and logs via `RemoteErrorHandler.log("ImageUpload", ...)`.

---

## Remote Data Sources

Located in `data/remote/source/`. Each interface mirrors the operations the repository needs:

| Interface | Table | Operations |
|---|---|---|
| `StoreRemoteDataSource` | `stores` | `getAll`, `getById`, `insert`, `update`, `delete` |
| `ProductRemoteDataSource` | `products` | `getByStore`, `getById`, `insert`, `update`, `delete` |
| `SaleRemoteDataSource` | `sales` | `getByStore`, `getById`, `insert`, `update`, `delete` |

Implementations in `data/remote/source/impl/` use `SupabaseClient.from(table)` directly. They are bound to their interfaces via `RemoteDataSourceModule` in `di/SupabaseModule.kt`.

All list queries apply `filter { eq("device_id", deviceIdProvider.id) }` so each device only retrieves its own rows.

All `insert()` calls in the implementations use Supabase `upsert()` — idempotent writes that replace existing rows on primary key conflict. This makes `pushAll()` and repository create/update operations safe to re-run without errors.

---

## Write Sync Strategy

Every repository write follows the same pattern:

```
1. Write to Room  →  returns immediately (works offline)
2. runCatching { push to Supabase }
   .onFailure { errorHandler.handle(...) }
```

- Room always succeeds first — the UI never waits for the network.
- Supabase failures are caught, logged, and emitted on `RemoteErrorHandler.errors` but do **not** fail the operation.
- There is no retry queue; a failed push is not retried automatically.

---

## Initial Sync (Fresh Install)

Handled by `data/sync/SyncManager`. Triggered from `PhoebeStoreApp.onCreate` on a background coroutine alongside `repairLocalImageUrls()`:

```kotlin
appScope.launch {
    syncManager.runInitialSyncIfNeeded()
    syncManager.repairLocalImageUrls()
}
```

**Detection:** a `SharedPreferences` boolean flag `initial_sync_done` (in `sync_prefs`). The flag is absent on fresh install (app data cleared on uninstall), so sync runs exactly once per installation.

`SyncManager` also exposes `isSyncing: StateFlow<Boolean>`, set to `true` at the start of the sync operation and back to `false` in a `try/finally` block, so the UI always reflects the correct state even if the sync throws.

**Decision tree:**

1. Fetch all stores from Supabase (filtered by `device_id`).
   - **Remote has data** → `pullAll()`: upsert stores, then for each store upsert its products and sales into Room.
   - **Remote is empty AND Room has data** → `pushAll()`: upload images via `ImageUploader.resolveUrl()`, stamp `deviceId` on each row, push all local stores, products, and sales to Supabase. Resolved HTTPS URLs are written back to Room before pushing.
   - **Both empty** → no-op.
2. **Flag is only set on full success.** If a pull or push fails mid-way the flag stays unset and the sync retries on the next launch. Upserts are idempotent, so partial data from a previous attempt is safe.

```
App start
   ├── SyncManager.runInitialSyncIfNeeded()
   │     ├── flag present? → skip
   │     └── flag absent?
   │           ├── remote has data? → pullAll()
   │           │       ├── success → set flag
   │           │       └── failure → notify() via RemoteErrorHandler, retry next launch
   │           ├── remote empty + local has data? → pushAll() [uploads images, stamps deviceId]
   │           │       ├── success → set flag
   │           │       └── failure → notify() via RemoteErrorHandler, retry next launch
   │           └── both empty → set flag (nothing to sync)
   └── SyncManager.repairLocalImageUrls()
```

---

## Image URL Repair

`SyncManager.repairLocalImageUrls()` runs on every app launch after `runInitialSyncIfNeeded()`. It recovers from a state where images were created locally but their upload to Supabase Storage failed (e.g. RLS blocked, bucket didn't exist yet), leaving `file://` URIs in Room and Supabase.

**Fast pre-scan:** Scans all store and product entities for any URL that starts with `file://`. If none are found the method returns immediately without showing the loading overlay or making network calls.

**Repair loop** (only runs when `file://` URLs are detected):
1. Sets `isSyncing = true` (shows the UI overlay).
2. For each store/product with a `file://` URL, calls `repairUrl()`:
   - If the local file still exists → uploads to Supabase Storage via `ImageUploader.resolveUrl()`, returns the HTTPS URL.
   - If the file is gone (deleted or reinstall) → returns `""` (blank). The composables show the placeholder icon for blank URLs.
3. Persists updated URLs to both Room (`dao.update()`) and Supabase (`remote.update()`). Supabase failures are caught via `runCatching` and logged with `errorHandler.log()` — repair of the local Room URL always succeeds regardless.
4. Sets `isSyncing = false` in `finally`.

---

## Error Handling

`data/sync/RemoteErrorHandler` is a Hilt singleton that centralises all remote error reporting.

Two distinct methods:

```kotlin
// Logcat only — used for background write failures (fire-and-forget)
fun log(operation: String, error: Throwable)

// Logcat + SharedFlow — used when a user-visible snackbar is appropriate
fun notify(operation: String, error: Throwable, userMessage: String)
```

Both log to Logcat under the `RemoteSync` tag: `[StoreCreate] <message>`.  
`notify()` additionally emits `userMessage` on `errors: SharedFlow<String>` (buffer of 8).

**Who uses which:**
- Repository write failures (`StoreCreate`, `StoreUpdate`, etc.) → `log()` — silent, background, no snackbar.
- `SyncManager` pull/push failures → `notify()` — surfaces a snackbar with a message like "Could not connect…", "Failed to restore…", or "Failed to back up…".
- Image repair failures (`StoreRepair`, `ProductRepair`) and upload failures (`ImageUpload`) → `log()` — silent; non-fatal.

Operations tagged: `StoreCreate`, `StoreUpdate`, `StoreDelete`, `ProductCreate`, `ProductUpdate`, `ProductDelete`, `SaleCreate`, `SaleUpdate`, `SaleDelete`, `InitialSync`, `StoreRepair`, `ProductRepair`, `ImageUpload`.

---

## Sync Feedback (SyncViewModel + AppNavigation)

`presentation/navigation/SyncViewModel.kt` bridges `SyncManager` and `RemoteErrorHandler` to the navigation layer:

```kotlin
@HiltViewModel
class SyncViewModel @Inject constructor(
    errorHandler: RemoteErrorHandler,
    syncManager: SyncManager
) : ViewModel() {
    val syncError: SharedFlow<String> = errorHandler.errors
    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
}
```

`AppNavigation` injects `SyncViewModel` and uses both properties:

- **Snackbar:** a `LaunchedEffect(Unit)` collects `syncError` and calls `snackbarHostState.showSnackbar(message)`. The `Scaffold` hosts a `SnackbarHost`.
- **Blur:** when `isSyncing = true`, the `NavHost` receives `Modifier.blur(16.dp)` — the current screen content is blurred behind the overlay.
- **Overlay:** an `AnimatedVisibility` (outside the `NavHost`, inside the root `Box`) is keyed on `isSyncing`. Enter: `scaleIn(initialScale = 0f, 400ms, FastOutSlowInEasing) + fadeIn(400ms)`. Exit: `scaleOut(targetScale = 0f, 300ms) + fadeOut(300ms)`. When visible it renders a full-screen `Box` with `surface.copy(alpha = 0.7f)` background and a centred 64dp `CircularProgressIndicator`.

This means sync feedback is **global** — no individual screen needs to handle it.

---

## User Settings (DataStore Preferences)

`domain/repository/UserSettingsRepository.kt` is a lightweight settings contract backed by Jetpack DataStore Preferences:

```kotlin
interface UserSettingsRepository {
    val lastAccessedStoreId: Flow<Long?>
    suspend fun setLastAccessedStore(storeId: Long)
}
```

**`lastAccessedStoreId`** — emits the ID of the last store the user explicitly visited. Emits `null` on a fresh install or after the app's data is cleared (DataStore file does not exist yet).

`UserSettingsRepositoryImpl` uses `preferencesDataStore(name = "user_settings")` (a top-level extension on `Context`) and stores the value under `longPreferencesKey("last_accessed_store_id")`.

**Where it is written:**
- `StoreDetailViewModel.init` — when the user opens a store's detail screen.
- `RecordSaleViewModel.init` — when the user opens the record-sale screen for a store.

**Where it is read:**
- Not currently read by any ViewModel. `HomeViewModel` drives its `uiState` from `storeRepository.getAll()` directly — see [HomeScreen](ui-screens.md#homescreen).

---

## Supabase Setup Requirements

These SQL statements and settings must be applied in the Supabase Dashboard when setting up a new project or adding these features.

### device_id columns

```sql
ALTER TABLE stores   ADD COLUMN IF NOT EXISTS device_id TEXT NOT NULL DEFAULT '';
ALTER TABLE products ADD COLUMN IF NOT EXISTS device_id TEXT NOT NULL DEFAULT '';
ALTER TABLE sales    ADD COLUMN IF NOT EXISTS device_id TEXT NOT NULL DEFAULT '';
```

### Row Level Security policies (anon key access)

By default Supabase enables RLS on all tables and the anon key cannot INSERT/UPDATE rows without explicit policies.

```sql
CREATE POLICY "anon_all_stores"   ON stores   FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "anon_all_products" ON products FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "anon_all_sales"    ON sales    FOR ALL TO anon USING (true) WITH CHECK (true);

-- Allows the anon key to upload and download Storage objects:
CREATE POLICY "anon_all_storage" ON storage.objects FOR ALL TO anon USING (true) WITH CHECK (true);
```

### Storage buckets

Create two public buckets (`public = true`, `image/*` MIME filter, 5 MB size limit):
- `store-images`
- `product-images`

Or via SQL after creating the buckets:

```sql
UPDATE storage.buckets SET public = true WHERE id IN ('store-images', 'product-images');
```
