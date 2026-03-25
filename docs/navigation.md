# Navigation

## Overview

Navigation uses Jetpack Navigation Compose with **type-safe serializable routes**. All routes are defined as `@Serializable` objects or data classes in `AppRoutes.kt`. The entire graph is wired in a single composable: `AppNavigation.kt`.

---

## Route Definitions (`AppRoutes.kt`)

| Route | Type | Parameters |
|---|---|---|
| `HomeScreen` | `data object` | — |
| `StoreListScreen` | `data object` | — |
| `StoreDetailScreen` | `data class` | `storeId: Long` |
| `CreateStoreScreen` | `data class` | `storeId: Long? = null` (null = create mode) |
| `CreateProductScreen` | `data class` | `storeId: Long`, `productId: Long? = null` (null = create mode) |
| `RecordSaleScreen` | `data class` | `storeId: Long` |

---

## Navigation Flow

```
HomeScreen
    ├─► StoreListScreen
    │       ├─► CreateStoreScreen()              (create new store)
    │       └─► StoreDetailScreen(storeId)
    │               ├─► CreateStoreScreen(storeId)          (edit store)
    │               ├─► ProductListScreen(storeId)
    │               │       ├─► CreateProductScreen(storeId)         (create product)
    │               │       └─► CreateProductScreen(storeId, productId)  (edit product)
    │               └─► RecordSaleScreen(storeId)           (log a sale)
    └─►(tap last store card)─► StoreDetailScreen(storeId)
```

All leaf actions (`onStoreSaved`, `onProductSaved`, `onSaleRecorded`) call `popBackStack()` to return to the previous screen.

## Transitions

All screen entries and exits use horizontal slide animations:

| Direction | Transition |
|---|---|
| Forward (navigate) | Slide in from right, previous screen slides out to left |
| Back (pop) | Current screen slides out to right, previous screen slides in from left |

Configured globally on the `NavHost` via `enterTransition`, `exitTransition`, `popEnterTransition`, `popExitTransition`.

---

## Screen Responsibilities

| Screen | Purpose |
|---|---|
| `HomeScreen` | Welcome overview with a summary of total sales and profits across all stores |
| `StoreListScreen` | Lists all stores created by the user; entry point to create a new store |
| `StoreDetailScreen` | Shows a single store's summary: products, inventory levels, sales history |
| `CreateStoreScreen` | Form to create or edit a store (determined by whether `storeId` is null) |
| `CreateProductScreen` | Form to create or edit a product within a store (determined by whether `productId` is null) |
| `RecordSaleScreen` | Form to log a new sale against a store's products |

---

## Back Stack Behaviour

- **Create/Edit screens** (`CreateStoreScreen`, `CreateProductScreen`, `RecordSaleScreen`): call `popBackStack()` on save, returning to the previous screen.
- **No screens are popped inclusively** at this stage; all destinations remain navigable via the system back button.
