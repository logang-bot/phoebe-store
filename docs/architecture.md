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
│                       Data                           │  ← Room, Retrofit (future), Hilt
│  data/local/  ·  data/remote/  ·  data/mapper/      │
│  data/repository/impl/                               │
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

### `data/`
Implements `domain/repository/` contracts. The only layer allowed to touch databases or network calls.

| Package | Contents |
|---|---|
| `data/local/entity/` | Room `@Entity` classes (suffixed `Entity`) |
| `data/local/dao/` | Room DAO interfaces |
| `data/local/AppDatabase.kt` | Room database definition |
| `data/remote/dto/` | `@Serializable` API response classes (suffixed `Dto`) |
| `data/mapper/` | Extension functions: `Entity → Domain`, `Dto → Domain`, `Domain → Entity` |
| `data/repository/impl/` | Concrete repository implementations |

Rule: never expose entities or DTOs above this layer — always map to domain models first.

### `ui/` & `presentation/`
The outermost layer. Owns everything the user sees.

| Package | Contents |
|---|---|
| `ui/screen/<feature>/` | Screen composable + ViewModel per feature |
| `ui/common/` | Shared composables and utilities (e.g. `PermissionDialog`) |
| `ui/theme/` | Material 3 theme — colors, typography, shapes |
| `presentation/navigation/` | Single `NavHost` graph wiring all screens |
| `presentation/screens/` | Type-safe `@Serializable` route definitions |

### `di/`
Hilt modules that wire everything at startup.

| File | Provides |
|---|---|
| `di/DatabaseModule.kt` | Room database, DAOs, repository bindings |

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
│   │   └── Product.kt
│   └── repository/
│       ├── StoreRepository.kt
│       └── ProductRepository.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── entity/
│   │   │   ├── StoreEntity.kt
│   │   │   └── ProductEntity.kt
│   │   └── dao/
│   │       ├── StoreDao.kt
│   │       └── ProductDao.kt
│   ├── remote/
│   │   └── dto/
│   │       ├── StoreDto.kt
│   │       └── ProductDto.kt
│   ├── mapper/
│   │   ├── StoreMapper.kt
│   │   └── ProductMapper.kt
│   └── repository/
│       └── impl/
│           ├── StoreRepositoryImpl.kt
│           └── ProductRepositoryImpl.kt
├── presentation/
│   ├── navigation/
│   │   └── AppNavigation.kt
│   └── screens/
│       └── AppRoutes.kt
├── ui/
│   ├── common/
│   │   ├── ActivityExtensions.kt
│   │   ├── PermissionDialog.kt
│   │   └── StoreCard.kt               ← shared card (HomeScreen + StoreListScreen)
│   ├── screen/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   └── StoreOverviewPlaceholder.kt
│   │   ├── store/
│   │   │   ├── StoreListScreen.kt
│   │   │   ├── StoreListViewModel.kt
│   │   │   ├── StoreDetailScreen.kt
│   │   │   ├── StoreDetailViewModel.kt
│   │   │   ├── CreateStoreScreen.kt
│   │   │   └── CreateStoreViewModel.kt
│   │   ├── product/
│   │   │   ├── ProductListScreen.kt
│   │   │   ├── ProductListViewModel.kt
│   │   │   ├── ProductCard.kt
│   │   │   ├── CreateProductScreen.kt
│   │   │   └── CreateProductViewModel.kt
│   │   └── sale/
│   │       └── RecordSaleScreen.kt     ← placeholder
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
6. The ViewModel emits a `StoreSaved` event via a `Channel`.
7. The screen collects the event and calls `onStoreSaved()`, which pops the back stack.
8. `StoreListScreen` is now visible. Its `StateFlow<List<Store>>` (backed by `StoreDao.getAll()`) automatically emits the updated list, and the new `StoreCard` appears with a `animateItem()` fade-in.
