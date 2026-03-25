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

### `AppDatabase`

```kotlin
@Database(entities = [StoreEntity::class, ProductEntity::class], version = 1)
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

---

## Dependency Injection

`di/DatabaseModule.kt` contains two Hilt modules:

**`DatabaseModule`** (object) — provides:
- `AppDatabase` singleton (Room builder with `fallbackToDestructiveMigration`)
- `StoreDao` and `ProductDao` (scoped to the database singleton)

**`RepositoryModule`** (abstract) — binds:
- `StoreRepositoryImpl` → `StoreRepository`
- `ProductRepositoryImpl` → `ProductRepository`
