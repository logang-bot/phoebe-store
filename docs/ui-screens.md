# UI Screens

## Conventions

All screens follow the **stateful / stateless split**:

- The **stateful** composable (`fun XScreen(viewModel = hiltViewModel())`) collects state from the ViewModel and delegates to the stateless version.
- The **stateless** composable (`private fun XScreenContent(...)`) takes plain values and lambdas — fully previewable without Hilt.

Every screen has **light and dark `@Preview`** variants. Screens with a meaningful empty state have an additional empty-state preview pair.

Strings are never hardcoded in composables — all user-visible text lives in `res/values/strings.xml` and is accessed via `stringResource()` / `stringArrayResource()`.

Colors are always taken from `MaterialTheme.colorScheme`. No hardcoded color values in UI code.

---

## Theme

### Fonts (`ui/theme/Type.kt`)

| Family | File | Used for |
|---|---|---|
| `displayFontFamily` | `funnel_display_variable.ttf` | Display, headline, title styles |
| `bodyFontFamily` | `nunito_sans_variable.ttf` + `nunito_sans_italic_variable.ttf` | Body, label styles |

Both are variable fonts — a single file covers all weights. The italic variable file is used for all italic styles of Nunito Sans.

### Colors (`ui/theme/Color.kt` + `Theme.kt`)

Full Material 3 color scheme with light, dark, medium-contrast, and high-contrast variants. Dynamic color is enabled on Android 12+ (API 31+). Below that, the static `lightScheme` / `darkScheme` is used.

---

## HomeScreen

**File:** `ui/screen/home/HomeScreen.kt`
**ViewModel:** `ui/screen/home/HomeViewModel.kt`

### Purpose
Entry point of the app. Shows a welcome greeting, a card for the last store the user created, a button to navigate to the store list, and an overview of that store's key metrics.

### State

`HomeViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `lastStore` | `StateFlow<Store?>` | Most recently created store; `null` if no stores exist yet |

### Components

#### `LastStoreCard` (`LastStoreCard.kt`)
Displays the last store in a card spanning almost the full width.

- **With a store:** `AsyncImage` (Coil) fills the card at 180dp height with a vertical gradient scrim (transparent → `surfaceContainerHigh`) so the store name and last-sale label remain legible over any photo. Name and last-sale time are anchored to the bottom-left of the image.
- **Without a store:** Centered empty-state text prompting the user to create their first store.

Previews: light / dark × with store / empty state (4 total).

#### `StoreOverviewPlaceholder` (`StoreOverviewPlaceholder.kt`)
Only rendered when a store exists. Shows a card with labelled rows for:
- Total sales
- Revenue
- Profit
- Products in stock
- Low stock alerts

All values are `—` until the `Sale` model and its data pipeline are implemented.

### Welcome message
A random entry from the `home_welcome_messages` string-array is selected once per composition via `remember { welcomeMessages.random() }`. The array is resolved via `stringArrayResource()` outside the `remember` block (composable context requirement).

---

## StoreListScreen

**File:** `ui/screen/store/StoreListScreen.kt`
**ViewModel:** `ui/screen/store/StoreListViewModel.kt`

### Purpose
Lists all stores created by the user. Entry point for creating a new store.

### State

`StoreListViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `stores` | `StateFlow<List<Store>>` | All stores, ordered by `createdAt DESC` |

### Layout
- Title at the top.
- `LazyColumn` of `StoreCard` items with `Modifier.animateItem()` — new stores animate in automatically when the Flow emits the updated list after creation.
- Full-width "Create new store" `Button` pinned at the bottom.
- Empty state shown when `stores` is empty.

### `StoreCard` (`StoreCard.kt`)
Clickable `Card` (uses `@ExperimentalMaterial3Api` `Card(onClick)`) that shows:
- `AsyncImage` at 160dp height with gradient scrim.
- Currency badge (top-right corner).
- Store name (bold) and optional description below the image.

---

## CreateStoreScreen

**File:** `ui/screen/store/CreateStoreScreen.kt`
**ViewModel:** `ui/screen/store/CreateStoreViewModel.kt`

### Purpose
Form to create a new store or edit an existing one. Determined by the `storeId` route parameter (`null` = create, non-null = edit).

### State

`CreateStoreViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `formState` | `CreateStoreFormState` | All form field values + validation flags + loading state |
| `visiblePermissionDialogQueue` | `SnapshotStateList<String>` | Permissions awaiting a dialog |
| `events` | `Flow<CreateStoreEvent>` | One-shot events (e.g. `StoreSaved`) |

`CreateStoreFormState` fields:

| Field | Default | Notes |
|---|---|---|
| `name` | `""` | Required — `nameError = true` if blank on save |
| `description` | `""` | Optional |
| `currency` | `Currency.USD` | Enum (`USD` / `BOB`) |
| `logoUrl` | `""` | Set after camera capture or gallery pick for the logo |
| `photoUrl` | `""` | Set after camera capture or gallery pick for the cover photo |
| `isLoading` | `false` | Save button shows `CircularProgressIndicator` when true |
| `nameError` | `false` | Shows supporting error text under the name field |

### Form fields
1. Store logo section — 120dp square frame (icon placeholder or `AsyncImage`) + "Take photo" / "Choose from gallery" buttons
2. Cover photo section — 16:9 `AsyncImage` preview (when set) + "Take photo" / "Choose from gallery" buttons
3. Store name (`OutlinedTextField`, required)
4. Description (`OutlinedTextField`, multiline 3–5 lines)
5. Currency (`ExposedDropdownMenuBox` listing `Currency.entries`)
6. Save `Button` (disabled while loading)

### Camera / photo flow
Both the logo and cover photo sections share a single `CAMERA` permission launcher. A `CameraTarget` (private screen-level enum: `LOGO` / `PHOTO`) tracks which field triggered the camera. A `cameraReadyToLaunch` state flag bridges the permission callback → `LaunchedEffect` → launcher call.

1. User taps "Take photo" for logo or cover photo → `pendingCameraTarget` set, permission checked.
2. If granted: `cameraReadyToLaunch = true` → `LaunchedEffect` fires, creates cache URI via `FileProvider`, launches the appropriate `TakePicture` launcher.
3. If not granted: `RequestPermission` launched → on grant `cameraReadyToLaunch = true` → same `LaunchedEffect` flow.
4. Denied result → `viewModel.onPermissionResult()` → dialog queue.
5. On capture: `viewModel.onLogoCaptured(uri)` or `viewModel.onPhotoCaptured(uri)` updates `formState`.

Gallery picks use `PickVisualMedia` (`ImageOnly`) — no extra permissions on API 33+. Separate launchers for logo and cover photo.

### Edit mode
When `storeId != null` the ViewModel loads the existing store via `SavedStateHandle` + `storeRepository.getById()` and pre-populates `formState`. On save, calls `storeRepository.update()` instead of `create()`.

### Navigation after save
ViewModel sends `CreateStoreEvent.StoreSaved` through a `Channel`. The screen collects it in a `LaunchedEffect` and calls `onStoreSaved()` → `navController.popBackStack()`. The `StoreListScreen` Flow then emits the updated list automatically.

---

## Placeholder Screens

The following screens exist as stubs and will be fully implemented in future iterations:

| Screen | File | Planned purpose |
|---|---|---|
| `StoreDetailScreen` | `ui/screen/store/StoreDetailScreen.kt` | Store summary: sales history, inventory, products |
| `CreateProductScreen` | `ui/screen/product/CreateProductScreen.kt` | Form to create or edit a product within a store |
| `RecordSaleScreen` | `ui/screen/sale/RecordSaleScreen.kt` | Form to log a new sale against a store's products |

---

## Common UI Components

### `PermissionDialog` (`ui/common/PermissionDialog.kt`)
Reusable `AlertDialog` for runtime permission rationale and permanent-denial flows. See [permission-handling.md](permission-handling.md).

### `ActivityExtensions.kt` (`ui/common/ActivityExtensions.kt`)
`Activity.openAppSettings()` — navigates the user to the app's system settings page.
