# PhoebeStore — Architecture

## Overview

PhoebeStore follows **Clean Architecture** with three layers. Dependencies only ever point inward — the domain layer has zero knowledge of Room, Compose, or any framework.

```
┌──────────────────────────────────────────────────────┐
│                   UI / Presentation                  │  ← Jetpack Compose, ViewModels, Hilt
│  ui/screen/  ·  ui/theme/  ·  ui/common/            │
│  presentation/navigation/  ·  presentation/screens/  │
├──────────────────────────────────────────────────────┤
│                      Domain                          │  ← Pure Kotlin, zero Android deps
│  domain/model/  ·  domain/repository/ (interfaces)  │
├──────────────────────────────────────────────────────┤
│                       Data                           │  ← Room, Supabase, Hilt
│  data/local/  ·  data/remote/  ·  data/mapper/      │
│  data/repository/impl/  ·  data/sync/               │
└──────────────────────────────────────────────────────┘
           ↑  dependencies point inward only  ↑
```

---

## Layer Responsibilities

### `domain/`
The innermost layer. Pure Kotlin — no Android, no Room, no Retrofit.

| Package | Contents |
|---|---|
| `domain/model/` | Plain Kotlin data classes representing core business concepts |
| `domain/repository/` | Repository **interfaces** — contracts the data layer must fulfill |
| `domain/usecase/` | Single-responsibility use cases that orchestrate multiple repositories |

### `data/`
Implements `domain/repository/` contracts. The only layer allowed to touch databases or network calls.

| Package | Contents |
|---|---|
| `data/local/entity/` | Room `@Entity` classes (suffixed `Entity`) |
| `data/local/dao/` | Room DAO interfaces |
| `data/local/AppDatabase.kt` | Room database definition |
| `data/remote/dto/` | `@Serializable` Supabase response classes (suffixed `Dto`) |
| `data/remote/source/` | Remote data source interfaces and implementations |
| `data/mapper/` | Extension functions: `Entity → Domain`, `Dto → Domain`, `Domain → Entity`, `Domain → Dto` |
| `data/repository/impl/` | Concrete repository implementations (offline-first: Room + Supabase) |
| `data/sync/` | `SyncManager` (initial pull on fresh install) and `RemoteErrorHandler` |

Rule: never expose entities or DTOs above this layer — always map to domain models first.

### `ui/` & `presentation/`
The outermost layer. Owns everything the user sees.

| Package | Contents |
|---|---|
| `ui/screen/<feature>/` | Screen composable, ViewModel, and state file(s) per feature |
| `ui/common/` | Shared composables and utilities (e.g. `PermissionDialog`) |
| `ui/theme/` | Material 3 theme — colors, typography, shapes |
| `presentation/navigation/` | Single `NavHost` graph wiring all screens |
| `presentation/screens/` | Type-safe `@Serializable` route definitions |

### `di/`
Hilt modules that wire everything at startup.

| File | Provides |
|---|---|
| `di/DatabaseModule.kt` | Room database, DAOs, repository bindings (including `UserSettingsRepository`) |
| `di/SupabaseModule.kt` | `SupabaseClient` singleton, remote data source bindings |

---

## Dependency Rule

```
UI / Presentation  →  Domain  ←  Data
                         ↑
                        di/
```

- `domain` has **no** dependencies on any other layer.
- `data` depends on `domain` (implements its interfaces).
- `ui` depends on `domain` (ViewModels call repository interfaces).
- `di` depends on all layers to wire them together at startup.

---

## Actual Folder Structure

```
app/src/main/java/com/example/phoebestore/
├── di/
│   └── DatabaseModule.kt
├── domain/
│   ├── model/
│   │   ├── Currency.kt
│   │   ├── Store.kt
│   │   ├── Product.kt
│   │   ├── Sale.kt                     ← onCredit + creditPersonName fields
│   │   ├── SaleType.kt
│   │   ├── ProfitOutcome.kt
│   │   └── InventoryLog.kt
│   ├── repository/
│   │   ├── StoreRepository.kt
│   │   ├── ProductRepository.kt
│   │   ├── SaleRepository.kt           ← update() + getOnCreditByStore()
│   │   ├── InventoryLogRepository.kt
│   │   └── UserSettingsRepository.kt   ← lastAccessedStoreId (DataStore)
│   └── usecase/
│       └── RecordSaleUseCase.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt              ← version 8
│   │   ├── entity/
│   │   │   ├── StoreEntity.kt
│   │   │   ├── ProductEntity.kt
│   │   │   ├── SaleEntity.kt           ← onCredit + creditPersonName columns
│   │   │   └── InventoryLogEntity.kt
│   │   └── dao/
│   │       ├── StoreDao.kt             ← upsert() added
│   │       ├── ProductDao.kt           ← upsert() added
│   │       ├── SaleDao.kt              ← upsert() added
│   │       └── InventoryLogDao.kt
│   ├── remote/
│   │   ├── dto/
│   │   │   ├── StoreDto.kt
│   │   │   ├── ProductDto.kt
│   │   │   └── SaleDto.kt              ← onCredit + creditPersonName added
│   │   └── source/
│   │       ├── StoreRemoteDataSource.kt
│   │       ├── ProductRemoteDataSource.kt
│   │       ├── SaleRemoteDataSource.kt
│   │       └── impl/
│   │           ├── StoreRemoteDataSourceImpl.kt
│   │           ├── ProductRemoteDataSourceImpl.kt
│   │           └── SaleRemoteDataSourceImpl.kt
│   ├── mapper/
│   │   ├── StoreMapper.kt              ← toDto() added
│   │   ├── ProductMapper.kt            ← toDto() added
│   │   ├── SaleMapper.kt               ← toDto() added
│   │   └── InventoryLogMapper.kt
│   ├── sync/
│   │   ├── SyncManager.kt              ← initial pull on fresh install
│   │   └── RemoteErrorHandler.kt       ← centralized network error logging
│   └── repository/
│       └── impl/
│           ├── StoreRepositoryImpl.kt  ← offline-first + Supabase sync
│           ├── ProductRepositoryImpl.kt
│           ├── SaleRepositoryImpl.kt
│           ├── InventoryLogRepositoryImpl.kt
│           └── UserSettingsRepositoryImpl.kt ← DataStore Preferences
├── presentation/
│   ├── navigation/
│   │   ├── AppNavigation.kt            ← Scaffold with snackbar + sync progress bar
│   │   └── SyncViewModel.kt            ← bridges SyncManager + RemoteErrorHandler to UI
│   └── screens/
│       └── AppRoutes.kt                ← CreditSalesListScreen route added
├── ui/
│   ├── common/
│   │   ├── ActivityExtensions.kt
│   │   ├── DateRangeFilter.kt
│   │   ├── LoadingButton.kt
│   │   ├── PermissionDialog.kt
│   │   ├── ProductDropdown.kt
│   │   ├── StoreCard.kt
│   │   └── ThemedCard.kt
│   ├── screen/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   ├── HomeUiState.kt
│   │   │   └── StoreOverviewPlaceholder.kt
│   │   ├── store/
│   │   │   ├── StoreListScreen.kt
│   │   │   ├── StoreListViewModel.kt
│   │   │   ├── StoreListUiState.kt
│   │   │   ├── StoreDetailScreen.kt    ← "Sales on Credit" button added
│   │   │   ├── StoreDetailViewModel.kt
│   │   │   ├── StoreDetailUiState.kt
│   │   │   ├── StoreDetailOverviewCard.kt
│   │   │   ├── CreateStoreScreen.kt
│   │   │   ├── CreateStoreViewModel.kt
│   │   │   └── CreateStoreFormState.kt
│   │   ├── product/
│   │   │   ├── ProductListScreen.kt
│   │   │   ├── ProductListViewModel.kt
│   │   │   ├── ProductListUiState.kt
│   │   │   ├── ProductCard.kt
│   │   │   ├── UpdateStockDialog.kt
│   │   │   ├── InventoryHistoryScreen.kt
│   │   │   ├── CreateProductScreen.kt
│   │   │   ├── CreateProductViewModel.kt
│   │   │   └── CreateProductFormState.kt
│   │   └── sale/
│   │       ├── RecordSaleScreen.kt
│   │       ├── RecordSaleViewModel.kt
│   │       ├── RecordSaleFormState.kt  ← isOnCredit + creditPersonName added
│   │       ├── SaleFormContent.kt      ← OnCreditSection added
│   │       ├── SearchTopBar.kt
│   │       ├── SearchResultsContent.kt
│   │       ├── SaleConfirmDialog.kt    ← shows credit row when applicable
│   │       ├── SaleResult.kt
│   │       ├── SaleResultDialog.kt
│   │       ├── SalePriceRow.kt
│   │       ├── SaleTotalSection.kt
│   │       ├── SaleModificationInfo.kt
│   │       ├── DateField.kt
│   │       ├── SalesListScreen.kt      ← CreditBadge chip added
│   │       ├── SalesListViewModel.kt
│   │       ├── SalesListUiState.kt     ← isOnCredit field in SaleDisplayItem
│   │       ├── SaleDetailScreen.kt
│   │       ├── SaleDetailViewModel.kt
│   │       ├── SaleDetailUiState.kt
│   │       ├── SalesReportScreen.kt    ← CreditSalesNote in TotalsSection
│   │       ├── SalesReportViewModel.kt ← computeCreditTotals()
│   │       ├── SalesReportUiState.kt   ← creditSalesCount + credit totals
│   │       ├── SalesReportCharts.kt
│   │       ├── CreditSalesListScreen.kt    ← new
│   │       ├── CreditSalesListViewModel.kt ← new
│   │       ├── CreditSalesListUiState.kt   ← new
│   │       └── CreditSaleGridItem.kt       ← new
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── PhoebeStoreApp.kt
└── MainActivity.kt
```

---

## Data Flow Example

**User creates a new store:**

1. `CreateStoreScreen` collects `formState` from `CreateStoreViewModel`.
2. User fills the form and taps Save.
3. `CreateStoreViewModel.saveStore()` validates the form, then calls `StoreRepository.create(store)`.
4. `StoreRepositoryImpl` maps the domain `Store` → `StoreEntity` via `StoreMapper`, then calls `StoreDao.insert()`.
5. Room inserts the row and returns the new ID.
6. The ViewModel emits a `CreateStoreEvent.StoreSaved` event via a `Channel`.
7. The screen collects the event in a `LaunchedEffect` and calls `onStoreSaved()`, which pops the back stack.
8. `StoreListScreen` is now visible. Its `StateFlow<StoreListUiState>` (backed by `StoreDao.getAll()`) automatically emits the updated list, and the new `StoreCard` appears with a `animateItem()` fade-in.
