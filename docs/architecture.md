# PhoebeStore — Clean Architecture

## Overview

This project follows **Clean Architecture** as described by Robert C. Martin, adapted for Android with Jetpack Compose. The goal is a strict separation of concerns where inner layers know nothing about outer layers, making the codebase testable, maintainable, and scalable.

```
┌────────────────────────────────────────────────┐
│                 Presentation                   │  ← Android / Compose
│  screens/ · navigation/ · ViewModels           │
├────────────────────────────────────────────────┤
│                   Domain                       │  ← Pure Kotlin
│  model/ · usecase/ · repository/ (interfaces) │
├────────────────────────────────────────────────┤
│                    Data                        │  ← Android / Retrofit / Room
│  remote/ · local/ · repository/ · mapper/     │
└────────────────────────────────────────────────┘
         ↑ dependencies point inward only ↑
```

---

## Layer Responsibilities

### domain/
The innermost layer. Contains **pure Kotlin** with zero Android dependencies.

| Package | Contents |
|---|---|
| `domain/model/` | Business entities — plain data classes that represent core concepts (e.g. `Product`, `Cart`, `Order`) |
| `domain/repository/` | Repository **interfaces** — contracts the data layer must fulfill (e.g. `ProductRepository`) |
| `domain/usecase/` | Use cases / interactors — one class per business action, each exposing a single `invoke` operator (e.g. `GetProductsUseCase`, `AddToCartUseCase`) |

Rules:
- No Android imports.
- No framework dependencies (no Retrofit, no Room).
- Use cases only depend on domain models and repository interfaces.

---

### data/
Implements the contracts defined in `domain/repository/`. This layer is the only one allowed to talk to external systems.

| Package | Contents |
|---|---|
| `data/remote/` | Retrofit API service interfaces and their response DTOs |
| `data/local/` | Room database, DAO interfaces, and entity classes |
| `data/mapper/` | Extension functions that convert DTOs/entities ↔ domain models |
| `data/repository/` | Concrete `Repository` implementations that coordinate remote and local sources |

Rules:
- Implements interfaces from `domain/repository/`.
- Never exposes DTOs or Room entities upward — always maps to domain models first.
- Handles caching strategy (network-first, cache-first, etc.) here.

---

### presentation/
The outermost layer. Owns everything the user sees and interacts with.

| Package | Contents |
|---|---|
| `presentation/screens/` | One sub-package per feature (e.g. `screens/home/`, `screens/product/`). Each contains a `Screen` composable and its `ViewModel`. |
| `presentation/navigation/` | The single Compose Navigation graph that wires all screens together. |
| `ui/theme/` | Shared Material 3 theme — colors, typography, shapes. |

Rules:
- ViewModels depend on use cases, never on repositories or data sources directly.
- Screen composables hold no business logic — they observe `UiState` and delegate events to the ViewModel.
- Navigation routes are defined as sealed objects/classes in `navigation/`.

---

### di/
Hilt modules that wire everything together. One module file per layer is a good starting point:

| File | Provides |
|---|---|
| `di/NetworkModule.kt` | Retrofit, OkHttp, API services |
| `di/DatabaseModule.kt` | Room database, DAOs |
| `di/RepositoryModule.kt` | Binds repository interfaces → implementations |

---

## Dependency Rule

> Source code dependencies must point **inward only**.

```
Presentation  →  Domain  ←  Data
                   ↑
                  di/
```

- `domain` has no dependencies on any other layer.
- `data` depends on `domain` (implements its interfaces).
- `presentation` depends on `domain` (calls its use cases).
- `di` depends on all layers so it can wire them at startup.

---

## Folder Structure

```
app/src/main/java/com/example/phoebestore/
├── di/
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── model/
│   │   └── Product.kt
│   ├── repository/
│   │   └── ProductRepository.kt
│   └── usecase/
│       └── GetProductsUseCase.kt
├── data/
│   ├── remote/
│   │   ├── ApiService.kt
│   │   └── dto/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   └── dao/
│   ├── mapper/
│   │   └── ProductMapper.kt
│   └── repository/
│       └── ProductRepositoryImpl.kt
├── presentation/
│   ├── navigation/
│   │   └── AppNavGraph.kt
│   └── screens/
│       └── home/
│           ├── HomeScreen.kt
│           └── HomeViewModel.kt
├── ui/
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt
```

---

## Data Flow Example

**User opens the product list screen:**

1. `HomeScreen` (composable) collects `uiState` from `HomeViewModel`.
2. `HomeViewModel` calls `GetProductsUseCase()` inside a coroutine.
3. `GetProductsUseCase` calls `ProductRepository.getProducts()` (the interface).
4. `ProductRepositoryImpl` fetches from `ApiService` (remote) and caches in Room (local).
5. The impl maps DTOs → domain `Product` models and returns them.
6. The use case returns the list; the ViewModel wraps it in `UiState.Success`.
7. The composable recomposes and renders the product list.
