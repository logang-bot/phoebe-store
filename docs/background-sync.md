# Background Sync Queue

## Overview

Every write operation (create, update, delete) follows a two-step pattern:

1. **Write to Room** — instant, always succeeds, UI updates immediately.
2. **Enqueue a sync op** — a row is appended to `pending_sync_ops` in Room, then a `SyncWorker` is scheduled via WorkManager.

The user never waits for the network. WorkManager fires the worker as soon as a connection is available, processes all pending ops, and retries on failure.

---

## Components

| Class | Package | Role |
|---|---|---|
| `SyncOperationEntity` | `data/local/entity/` | Room row representing one pending Supabase call |
| `SyncOperationDao` | `data/local/dao/` | Insert / read all / delete by id |
| `SyncScheduler` | `data/sync/` | Inserts the op into Room and schedules the worker |
| `SyncWorker` | `data/sync/` | WorkManager worker — drains the queue, delegates dispatch to syncers |
| `EntitySyncer` | `data/sync/` | Interface: one implementation per entity type |
| `StoreSyncer` | `data/sync/` | Handles Store write/delete against Supabase |
| `ProductSyncer` | `data/sync/` | Handles Product write/delete against Supabase |
| `SaleSyncer` | `data/sync/` | Handles Sale write/delete against Supabase |
| `SyncerRegistry` | `data/sync/` | Maps `entityType` strings to the correct `EntitySyncer` |
| `SyncNotifier` | `data/sync/` | Posts local notifications for sync started / success / failure |

---

## `SyncOperationEntity`

```kotlin
@Entity(tableName = "pending_sync_ops")
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,   // "STORE" | "PRODUCT" | "SALE"
    val entityId: Long,       // Room PK of the affected row
    val operation: String,    // "CREATE" | "UPDATE" | "DELETE"
    val createdAt: Long = System.currentTimeMillis()
)
```

**Why no request body?** The syncer reads the entity from Room at dispatch time. Room is the source of truth, so the syncer always sends the latest state — even if the entity was updated again between the enqueue and the dispatch.

**Why no endpoint URL?** `entityType` alone determines which `EntitySyncer` to use. Each syncer knows which remote data source and Supabase table to call — that detail lives inside the syncer, not in the queue.

---

## Queue flow

```
User action (e.g. RecordSaleScreen taps "Save")
  │
  ▼
SaleRepositoryImpl.create(sale)
  ├── dao.insert(sale.toEntity())         ← Room write, instant
  └── syncScheduler.enqueue("SALE", id, "CREATE")
        ├── syncOpDao.insert(SyncOperationEntity(...))
        └── SyncWorker.schedule(workManager)
              └── enqueueUniqueWork("SyncWorker", KEEP, constraints=CONNECTED)
```

The coroutine returns after the Room write. UI proceeds immediately.

```
Later — when network is available:

WorkManager fires SyncWorker.doWork()
  └── syncOpDao.getAll()  →  [op1, op2, ...]
        for each op:
          ├── dispatch(op)
          │     └── syncerRegistry.get(op.entityType)  →  EntitySyncer
          │           ├── OP_CREATE / OP_UPDATE → syncer.syncWrite(entityId)
          │           └── OP_DELETE             → syncer.syncDelete(entityId)
          │     success → syncOpDao.deleteById(op.id)
          │     failure → keep in queue, errorHandler.log(...)
          └── (repeat)
        allSucceeded? → Result.success()
        anyFailed?    → Result.retry()   ← WorkManager retries with exponential backoff
```

---

## EntitySyncer — strategy pattern

`SyncWorker` has zero knowledge of entity types, DAOs, remote data sources, or image upload logic. All of that lives in the syncer implementations.

```kotlin
interface EntitySyncer {
    suspend fun syncWrite(entityId: Long)   // handles CREATE and UPDATE
    suspend fun syncDelete(entityId: Long)
}
```

`CREATE` and `UPDATE` map to the same `syncWrite()` because all Supabase writes use `upsert()` — they replace on primary key conflict. This makes every op idempotent and safe to retry.

`SyncerRegistry` maps the `entityType` string to the right implementation:

```kotlin
fun get(entityType: String): EntitySyncer? = when (entityType) {
    TYPE_STORE   -> storeSyncer
    TYPE_PRODUCT -> productSyncer
    TYPE_SALE    -> saleSyncer
    else         -> null
}
```

Adding support for a new entity type only requires a new `EntitySyncer` implementation and one line in `SyncerRegistry.get()` — the worker itself does not change.

---

## Dispatch logic per syncer

| Syncer | `syncWrite` | `syncDelete` |
|---|---|---|
| `StoreSyncer` | Read `StoreEntity` from Room → upload images (if `file://`) → update Room with HTTPS URLs → `storeRemote.insert(dto)` | `storeRemote.delete(entityId)` |
| `ProductSyncer` | Read `ProductEntity` from Room → upload image (if `file://`) → update Room with HTTPS URL → `productRemote.insert(dto)` | `productRemote.delete(entityId)` |
| `SaleSyncer` | Read `SaleEntity` from Room → `saleRemote.insert(dto)` | `saleRemote.delete(entityId)` |

**Null entity (entity deleted before worker ran):** `syncWrite()` returns normally without throwing when `getById()` returns null, so the op is removed from the queue. This handles CASCADE deletes — e.g. deleting a store removes its products/sales from Room, leaving orphaned CREATE/UPDATE ops that should simply be discarded.

---

## Supabase table mapping

The remote data sources call Supabase PostgREST via the Kotlin Supabase client. There are no raw URLs built in the worker or syncers — the data source implementations handle the table name:

| `entityType` | Syncer | Remote data source | Supabase table |
|---|---|---|---|
| `STORE` | `StoreSyncer` | `StoreRemoteDataSource` | `stores` |
| `PRODUCT` | `ProductSyncer` | `ProductRemoteDataSource` | `products` |
| `SALE` | `SaleSyncer` | `SaleRemoteDataSource` | `sales` |

The Supabase client constructs the HTTP request internally. For example, `saleRemote.insert(dto)` translates to:

```
POST https://<ref>.supabase.co/rest/v1/sales
Prefer: resolution=merge-duplicates   ← upsert behaviour
Authorization: Bearer <anon-key>
Content-Type: application/json

{ "id": 42, "store_id": 1, "product_name": "...", ... }
```

---

## Image upload

`StoreSyncer` and `ProductSyncer` call `ImageUploader.resolveUrl()` inside `syncWrite()` before pushing to Supabase:

- Returns the URL unchanged if it is already `https://` (already uploaded).
- Uploads the local file to Supabase Storage and returns the public HTTPS URL if the URL starts with `file://`.
- Updates the Room entity with the HTTPS URL so subsequent reads see it immediately.

Storage paths:

| Syncer | Field | Storage path |
|---|---|---|
| `StoreSyncer` | `logoUrl` | `store-images/logos/<deviceId>/<storeId>.jpg` |
| `StoreSyncer` | `photoUrl` | `store-images/photos/<deviceId>/<storeId>.jpg` |
| `ProductSyncer` | `imageUrl` | `product-images/products/<deviceId>/<productId>.jpg` |

---

## WorkManager configuration

```kotlin
OneTimeWorkRequestBuilder<SyncWorker>()
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
    .addTag("SyncWorker")
    .build()

workManager.enqueueUniqueWork("SyncWorker", ExistingWorkPolicy.KEEP, request)
```

- **`CONNECTED` constraint** — the worker never runs without a network. WorkManager monitors connectivity via `ConnectivityManager.NetworkCallback` and fires the worker automatically when the device reconnects, with no code needed on the app side.
- **`ExistingWorkPolicy.KEEP`** — if a worker is already queued or running, new enqueue calls are ignored. The running worker will pick up any newly inserted ops because it reads the queue at execution time.
- **Exponential backoff starting at 30 s** — on failure WorkManager retries at 30 s, 60 s, 120 s, …, capped at the WorkManager default maximum (5 hours).
- **Scheduled on every app start** (`PhoebeStoreApp.onCreate`) as a safety net — ensures orphaned ops are drained if WorkManager's internal state ever diverges from the `pending_sync_ops` table. The `KEEP` policy makes this a no-op if work is already pending.

---

## Hilt + WorkManager wiring

`SyncWorker` is annotated with `@HiltWorker` and uses `@AssistedInject`. WorkManager uses a custom `HiltWorkerFactory` instead of the default factory so it can inject Hilt-managed dependencies into the worker.

`PhoebeStoreApp` implements `Configuration.Provider`:

```kotlin
@HiltAndroidApp
class PhoebeStoreApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

The default `WorkManagerInitializer` (AndroidX Startup content provider) is disabled in `AndroidManifest.xml` so WorkManager initialises lazily on first use — at which point `workerFactory` is already injected:

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="@string/androidx_startup"
        tools:node="remove" />
</provider>
```

---

## Local notifications

`SyncNotifier` is a `@Singleton` injected into `SyncWorker`. It posts a system notification for each of the three sync states:

| State | Notification | Behaviour |
|---|---|---|
| Started | "Syncing your changes…" + indeterminate progress bar | `setOngoing(true)` — not dismissible while sync is running |
| Success | "Changes saved" | `setAutoCancel(true)` — dismissed when tapped |
| Failure | "Sync failed" + "Will retry when back online" | `setAutoCancel(true)` — WorkManager will retry automatically |

All three states share the same `NOTIFICATION_ID` (1001) so each update replaces the previous notification rather than stacking.

The notification channel (`sync_channel`) uses `IMPORTANCE_LOW` — no sound or vibration, shown silently in the shade. The channel is created in `PhoebeStoreApp.onCreate()` via `syncNotifier.createChannel()`, which is a no-op if the channel already exists.

**Android 13+ permission (`POST_NOTIFICATIONS`):** declared in `AndroidManifest.xml`. `SyncNotifier.show()` checks `ContextCompat.checkSelfPermission` at runtime and silently skips the notification if the permission has not been granted. The permission must be requested from the user at an appropriate moment in the UI (e.g. first time the user records a sale) using the standard `ActivityResultContracts.RequestPermission` flow.

---

## Relation to other sync mechanisms

| Mechanism | Trigger | Purpose |
|---|---|---|
| `SyncWorker` (this) | Every repository write + every app start | Push individual changes to Supabase in the background |
| `SyncManager.runInitialSyncIfNeeded()` | App start, once per install | Pull remote data on fresh install or push local data to a new Supabase project |
| `SyncManager.repairLocalImageUrls()` | Every app start | Fix `file://` image URLs left over from failed uploads |

These three mechanisms are independent and complementary. `repairLocalImageUrls()` acts as a safety net for images that `SyncWorker` may have failed to upload (e.g. network was down when the worker ran).

---

## Database migration

`SyncOperationEntity` was introduced in database version 10:

```kotlin
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pending_sync_ops (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                entityType TEXT NOT NULL,
                entityId INTEGER NOT NULL,
                operation TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """)
    }
}
```
