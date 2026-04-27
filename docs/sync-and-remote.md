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

The `SupabaseClient` is provided as a Hilt singleton in `di/SupabaseModule.kt` with the `Postgrest` plugin installed. Ktor OkHttp engine is used for HTTP (reuses the OkHttp instance already present via Coil).

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
    sold_at bigint not null,
    created_at bigint not null
);
```

> All timestamps are Unix milliseconds, matching Room's `Long` columns.

> `lastAccessedAt` (Store) is local-only and has no Supabase column.

---

## Remote Data Sources

Located in `data/remote/source/`. Each interface mirrors the operations the repository needs:

| Interface | Table | Operations |
|---|---|---|
| `StoreRemoteDataSource` | `stores` | `getAll`, `getById`, `insert`, `update`, `delete` |
| `ProductRemoteDataSource` | `products` | `getByStore`, `getById`, `insert`, `update`, `delete` |
| `SaleRemoteDataSource` | `sales` | `getByStore`, `getById`, `insert`, `update`, `delete` |

Implementations in `data/remote/source/impl/` use `SupabaseClient.from(table)` directly. They are bound to their interfaces via `RemoteDataSourceModule` in `di/SupabaseModule.kt`.

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

Handled by `data/sync/SyncManager`. Triggered from `PhoebeStoreApp.onCreate` on a background coroutine.

**Detection:** a `SharedPreferences` boolean flag `initial_sync_done` (in `sync_prefs`). The flag is absent on fresh install (app data cleared on uninstall), so sync runs exactly once per installation.

`SyncManager` also exposes `isSyncing: StateFlow<Boolean>`, set to `true` at the start of the sync operation and back to `false` in a `try/finally` block, so the UI always reflects the correct state even if the sync throws.

**Decision tree:**

1. Fetch all stores from Supabase.
   - **Remote has data** → `pullAll()`: upsert stores, then for each store upsert its products and sales into Room.
   - **Remote is empty AND Room has data** → `pushAll()`: push all local stores, products, and sales to Supabase (covers existing users upgrading to a version with Supabase for the first time).
   - **Both empty** → no-op.
2. **Flag is only set on full success.** If a pull or push fails mid-way the flag stays unset and the sync retries on the next launch. Upserts are idempotent, so partial data from a previous attempt is safe.

```
App start
   └── SyncManager.runInitialSyncIfNeeded()
         ├── flag present? → skip
         └── flag absent?
               ├── remote has data? → pullAll()
               │       ├── success → set flag
               │       └── failure → notify() via RemoteErrorHandler, retry next launch
               ├── remote empty + local has data? → pushAll()
               │       ├── success → set flag
               │       └── failure → notify() via RemoteErrorHandler, retry next launch
               └── both empty → set flag (nothing to sync)
```

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

Operations tagged: `StoreCreate`, `StoreUpdate`, `StoreDelete`, `ProductCreate`, `ProductUpdate`, `ProductDelete`, `SaleCreate`, `SaleUpdate`, `SaleDelete`, `InitialSync`.

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
- **Progress indicator:** `isSyncing` is collected as state; when `true`, a `LinearProgressIndicator` is rendered at the top of the content `Box`, overlapping the `NavHost`.

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
- `HomeViewModel` — drives the `uiState` flow. A `null` result means no store has ever been explicitly visited, so `lastStore` is never set and the auto-navigation to `RecordSaleScreen` is blocked at the source (the `LaunchedEffect` guard in `HomeScreen` returns early when `lastStore == null`).

This prevents auto-navigation from firing on a fresh install before the user has ever entered a store, even if Room already contains data from a sync pull.
