# Data & Domain Layers

## Domain Models

Plain Kotlin data classes with no framework dependencies. These are the only model types that ViewModels and UI ever see.

### `Currency`

```kotlin
enum class Currency { USD, BOB }
```

### `Store`

```kotlin
data class Store(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val currency: Currency = Currency.USD,
    val logoUrl: String = "",
    val photoUrl: String = "",          // cover photo for store cards
    val createdAt: Long = System.currentTimeMillis()
)
```

### `Product`

```kotlin
data class Product(
    val id: Long = 0,
    val storeId: Long,                  // FK → Store
    val name: String,
    val description: String = "",
    val price: Double,                  // selling price
    val costPrice: Double = 0.0,        // purchase/acquisition cost — used for profit calculations
    val stock: Int = 0,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

> `price - costPrice` gives the gross profit margin per unit, which feeds into sales reports.

### `Sale`

```kotlin
data class Sale(
    val id: Long = 0,
    val storeId: Long,
    val productId: Long? = null,        // null for custom (ad-hoc) products
    val productName: String,            // snapshot of the name at sale time
    val quantity: Int,
    val unitPrice: Double,              // price charged per unit at sale time
    val unitCost: Double = 0.0,         // cost per unit at sale time (optional)
    val totalAmount: Double,            // unitPrice × quantity
    val saleType: SaleType = SaleType.STANDARD,
    val profitOutcome: ProfitOutcome = ProfitOutcome.NORMAL_PROFIT,
    val notes: String = "",
    val onCredit: Boolean = false,      // true when payment has not been collected yet
    val creditPersonName: String = "",  // name of the customer who owes payment
    val soldAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
```

> `unitPrice` and `unitCost` are **snapshots** recorded at sale time. They may differ from the product's current `price` / `costPrice` if the user adjusts them before saving — this is tracked via `saleType` and `profitOutcome`.

> When `onCredit = true` the sale has been recorded and stock has been decremented, but payment is still pending. `creditPersonName` identifies who owes the payment. Once paid, `onCredit` is set to `false` via `SaleRepository.update()` and the sale is removed from the credit sales view.

### `SaleType`

```kotlin
enum class SaleType { STANDARD, MODIFIED }
```

`STANDARD` — the sale used the product's catalogue prices unchanged.
`MODIFIED` — the user changed `unitPrice` and/or `unitCost` before saving.

### `ProfitOutcome`

```kotlin
enum class ProfitOutcome { NORMAL_PROFIT, EXTRA_PROFIT, SMALLER_PROFIT, LOSS }
```

| Value | Meaning |
|---|---|
| `NORMAL_PROFIT` | No modification, or modified prices yield the same margin |
| `EXTRA_PROFIT` | Modified margin is higher than the catalogue margin |
| `SMALLER_PROFIT` | Modified margin is lower than catalogue but still positive |
| `LOSS` | `unitPrice ≤ unitCost` — selling at a loss |

---

## Three-Model Pattern

Each business concept exists in three forms to keep layers cleanly separated:

| Variant | Package | Purpose | Framework |
|---|---|---|---|
| **Domain model** | `domain/model/` | Flows through the entire app above the data layer | None |
| **Room entity** | `data/local/entity/` | Persisted to SQLite via Room | Room |
| **API DTO** | `data/remote/dto/` | Serialised/deserialised from the REST API | kotlinx.serialization |

Mappers in `data/mapper/` convert between these representations.

---

## Mappers

Located in `data/mapper/`. All mappers are extension functions.

### `StoreMapper.kt`

| Function | Direction |
|---|---|
| `StoreEntity.toDomain(): Store` | Local DB → app |
| `StoreDto.toDomain(): Store` | API response → app |
| `Store.toEntity(): StoreEntity` | app → Local DB write |

### `ProductMapper.kt`

| Function | Direction |
|---|---|
| `ProductEntity.toDomain(): Product` | Local DB → app |
| `ProductDto.toDomain(): Product` | API response → app |
| `Product.toEntity(): ProductEntity` | app → Local DB write |

### `SaleMapper.kt`

| Function | Direction |
|---|---|
| `SaleEntity.toDomain(): Sale` | Local DB → app |
| `SaleDto.toDomain(): Sale` | API response → app |
| `Sale.toEntity(): SaleEntity` | app → Local DB write |

`SaleType` and `ProfitOutcome` are stored as strings in both entity and DTO. The mapper deserialises them with a safe fallback:

```kotlin
saleType = runCatching { SaleType.valueOf(saleType) }.getOrDefault(SaleType.STANDARD),
profitOutcome = runCatching { ProfitOutcome.valueOf(profitOutcome) }.getOrDefault(ProfitOutcome.NORMAL_PROFIT)
```

This prevents crashes if an unknown value arrives from the API or an older database schema.

---

## Room Entities

### `StoreEntity`

Table: `stores`

| Column | Type | Notes |
|---|---|---|
| `id` | `Long` | Primary key, auto-generated |
| `name` | `String` | |
| `description` | `String` | Default `""` |
| `currency` | `String` | Default `"USD"` |
| `logoUrl` | `String` | Default `""` |
| `photoUrl` | `String` | Default `""` — cover photo |
| `createdAt` | `Long` | Unix timestamp (ms) |

### `ProductEntity`

Table: `products`

| Column | Type | Notes |
|---|---|---|
| `id` | `Long` | Primary key, auto-generated |
| `storeId` | `Long` | FK → `stores.id` (CASCADE delete), indexed |
| `name` | `String` | |
| `description` | `String` | Default `""` |
| `price` | `Double` | Selling price |
| `costPrice` | `Double` | Acquisition cost — default `0.0` |
| `stock` | `Int` | Current inventory count |
| `imageUrl` | `String` | Default `""` |
| `createdAt` | `Long` | Unix timestamp (ms) |

> Deleting a `StoreEntity` cascades and deletes all its `ProductEntity` rows.

### `SaleEntity`

Table: `sales`

| Column | Type | Notes |
|---|---|---|
| `id` | `Long` | Primary key, auto-generated |
| `storeId` | `Long` | FK → `stores.id` (CASCADE delete), indexed |
| `productId` | `Long?` | FK → `products.id`; `null` for custom products |
| `productName` | `String` | Snapshot of name at sale time |
| `quantity` | `Int` | Units sold |
| `unitPrice` | `Double` | Price per unit at sale time |
| `unitCost` | `Double` | Cost per unit at sale time; default `0.0` |
| `totalAmount` | `Double` | `unitPrice × quantity` |
| `saleType` | `String` | `"STANDARD"` or `"MODIFIED"` |
| `profitOutcome` | `String` | `"NORMAL_PROFIT"`, `"EXTRA_PROFIT"`, `"SMALLER_PROFIT"`, or `"LOSS"` |
| `notes` | `String` | Default `""` |
| `onCredit` | `Boolean` | `false` by default; `true` means payment is pending |
| `creditPersonName` | `String` | Name of the customer who owes payment; required when `onCredit = true` |
| `soldAt` | `Long` | Unix timestamp (ms) — when the sale occurred |
| `createdAt` | `Long` | Unix timestamp (ms) — when the record was created |

> Deleting a `StoreEntity` cascades and deletes all its `SaleEntity` rows.

### `AppDatabase`

```kotlin
@Database(entities = [StoreEntity::class, ProductEntity::class, SaleEntity::class, InventoryLogEntity::class], version = 8)
```

`fallbackToDestructiveMigration()` is enabled — safe during development, replace with proper migrations before production.

---

## API DTOs

Located in `data/remote/dto/`. Annotated with `@Serializable` (kotlinx.serialization). Field names use `@SerialName` to match the API's snake_case convention.

### `StoreDto`

| JSON field | Kotlin property |
|---|---|
| `id` | `id: Long` |
| `name` | `name: String` |
| `description` | `description: String` |
| `currency` | `currency: String` |
| `logo_url` | `logoUrl: String` |
| `photo_url` | `photoUrl: String` |
| `created_at` | `createdAt: Long` |

### `ProductDto`

| JSON field | Kotlin property |
|---|---|
| `id` | `id: Long` |
| `store_id` | `storeId: Long` |
| `name` | `name: String` |
| `description` | `description: String` |
| `price` | `price: Double` |
| `cost_price` | `costPrice: Double` |
| `stock` | `stock: Int` |
| `image_url` | `imageUrl: String` |
| `created_at` | `createdAt: Long` |

### `SaleDto`

| JSON field | Kotlin property |
|---|---|
| `id` | `id: Long` |
| `store_id` | `storeId: Long` |
| `product_id` | `productId: Long?` |
| `product_name` | `productName: String` |
| `quantity` | `quantity: Int` |
| `unit_price` | `unitPrice: Double` |
| `unit_cost` | `unitCost: Double` |
| `total_amount` | `totalAmount: Double` |
| `sale_type` | `saleType: String` |
| `profit_outcome` | `profitOutcome: String` |
| `notes` | `notes: String` |
| `sold_at` | `soldAt: Long` |
| `created_at` | `createdAt: Long` |

---

## DAOs

### `StoreDao`

| Method | Returns | Notes |
|---|---|---|
| `insert(store)` | `Long` | Inserted row ID; aborts on conflict |
| `update(store)` | `Unit` | |
| `getById(id)` | `StoreEntity?` | Suspending |
| `getAll()` | `Flow<List<StoreEntity>>` | Ordered by `createdAt DESC` |
| `deleteById(id)` | `Unit` | |

### `ProductDao`

| Method | Returns | Notes |
|---|---|---|
| `insert(product)` | `Long` | Inserted row ID; aborts on conflict |
| `update(product)` | `Unit` | |
| `getById(id)` | `ProductEntity?` | Suspending |
| `getByStore(storeId)` | `Flow<List<ProductEntity>>` | Ordered by `name ASC` |
| `deleteById(id)` | `Unit` | |

### `SaleDao`

| Method | Returns | Notes |
|---|---|---|
| `insert(sale)` | `Long` | Inserted row ID; aborts on conflict |
| `update(sale)` | `Unit` | Full-row update (used to mark credit sales as paid) |
| `getById(id)` | `SaleEntity?` | Suspending |
| `getByStore(storeId)` | `Flow<List<SaleEntity>>` | Ordered by `soldAt DESC` |
| `getOnCreditByStore(storeId)` | `Flow<List<SaleEntity>>` | Only rows where `onCredit = 1`, ordered by `soldAt DESC` |
| `deleteById(id)` | `Unit` | |

---

## Repository Interfaces

Defined in `domain/repository/` — the contract the data layer must fulfill. ViewModels depend only on these interfaces, never on implementations.

### `StoreRepository`

```kotlin
interface StoreRepository {
    suspend fun create(store: Store): Long
    suspend fun update(store: Store)
    suspend fun getById(id: Long): Store?
    fun getAll(): Flow<List<Store>>
    suspend fun delete(id: Long)
}
```

### `ProductRepository`

```kotlin
interface ProductRepository {
    suspend fun create(product: Product): Long
    suspend fun update(product: Product)
    suspend fun getById(id: Long): Product?
    fun getByStore(storeId: Long): Flow<List<Product>>
    suspend fun delete(id: Long)
}
```

### `SaleRepository`

```kotlin
interface SaleRepository {
    suspend fun create(sale: Sale): Long
    suspend fun update(sale: Sale)
    suspend fun getById(id: Long): Sale?
    fun getByStore(storeId: Long): Flow<List<Sale>>
    fun getOnCreditByStore(storeId: Long): Flow<List<Sale>>
    suspend fun delete(id: Long)
}
```

`update()` is used by `CreditSalesListViewModel` to flip `onCredit = false` when a customer pays. It performs a full-row update via Room `@Update`.

`getOnCreditByStore()` returns only sales where `onCredit = true`, backed by the `getOnCreditByStore` DAO query. The result is a live `Flow` so the credit sales screen updates automatically after `update()` is called.

---

## Dependency Injection

`di/DatabaseModule.kt` contains two Hilt modules:

**`DatabaseModule`** (object) — provides:
- `AppDatabase` singleton (Room builder with `fallbackToDestructiveMigration`)
- `StoreDao`, `ProductDao`, and `SaleDao` (scoped to the database singleton)

**`RepositoryModule`** (abstract) — binds:
- `StoreRepositoryImpl` → `StoreRepository`
- `ProductRepositoryImpl` → `ProductRepository`
- `SaleRepositoryImpl` → `SaleRepository`
