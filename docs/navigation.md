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
| `SalesListScreen` | `data class` | `storeId: Long` |
| `SaleDetailScreen` | `data class` | `saleId: Long` |
| `SalesReportScreen` | `data class` | `storeId: Long`, `fromDate: Long`, `toDate: Long`, `productId: Long? = null` |
| `InventoryHistoryScreen` | `data class` | `storeId: Long` |
| `CreditSalesListScreen` | `data class` | `storeId: Long` |

---

## Navigation Flow

```
HomeScreen
    ├─► StoreListScreen
    │       ├─► CreateStoreScreen()              (create new store)
    │       └─► StoreDetailScreen(storeId)
    │               ├─► CreateStoreScreen(storeId)          (edit store)
    │               ├─► ProductListScreen(storeId)
    │               │       ├─► CreateProductScreen(storeId)           (create product)
    │               │       └─► CreateProductScreen(storeId, productId)(edit product)
    │               ├─► SalesListScreen(storeId)
    │               │       ├─► SaleDetailScreen(saleId)
    │               │       └─► SalesReportScreen(storeId, from, to, productId?)
    │               ├─► CreditSalesListScreen(storeId)
    │               ├─► InventoryHistoryScreen(storeId)
    │               └─► RecordSaleScreen(storeId)           (log a sale)
    └─►(tap last store card)─► StoreDetailScreen(storeId)
```

All leaf actions (`onStoreSaved`, `onProductSaved`, `onSaleRecorded`, `onNavigateBack`) call `popBackStack()` to return to the previous screen.

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
| `StoreDetailScreen` | Shows a single store's summary: products, inventory levels, and navigation hub to all store sub-features |
| `CreateStoreScreen` | Form to create or edit a store (determined by whether `storeId` is null) |
| `CreateProductScreen` | Form to create or edit a product within a store (determined by whether `productId` is null) |
| `RecordSaleScreen` | Form to log a new sale against a store's products, with optional on-credit marking |
| `SalesListScreen` | Paginated, date-filtered list of all sales for a store; credit sales are labelled with a badge |
| `SaleDetailScreen` | Read-only detail view of a single sale record |
| `SalesReportScreen` | Aggregated analytics report for a date range, including an on-credit breakdown note |
| `InventoryHistoryScreen` | Chronological log of all stock changes for a store |
| `CreditSalesListScreen` | Date-filtered 2-column grid of pending credit sales; tap an item to mark it as paid |

---

## Global Sync UI

`AppNavigation` injects `SyncViewModel` and handles two cross-cutting concerns that apply to the entire navigation graph:

- **Snackbar host**: the `Scaffold`'s `snackbarHost` slot is wired to a `SnackbarHostState`. A `LaunchedEffect(Unit)` collects `syncViewModel.syncError` (a `SharedFlow<String>`) and shows each message as a `Snackbar`. This is the surface point for all `RemoteErrorHandler.notify()` calls (e.g. initial sync failures).
- **Progress indicator**: `syncViewModel.isSyncing` is collected as state. When `true`, a `LinearProgressIndicator` is placed at the top of the content `Box`, above the `NavHost`. It disappears automatically when sync completes.

Neither concern is handled by individual screens — everything is centralised in `AppNavigation`.

---

## Back Stack Behaviour

- **Create/Edit screens** (`CreateStoreScreen`, `CreateProductScreen`, `RecordSaleScreen`): call `popBackStack()` on save, returning to the previous screen.
- **List/detail screens** (`SalesListScreen`, `SaleDetailScreen`, `SalesReportScreen`, `InventoryHistoryScreen`, `CreditSalesListScreen`): call `popBackStack()` via the top-bar back arrow.
- **No screens are popped inclusively** at this stage; all destinations remain navigable via the system back button.
