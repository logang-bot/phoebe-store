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

Two semantic warning colors are defined for use in profit/loss indicators:

| Token | Value | Used in |
|---|---|---|
| `warningLight` | `Color(0xFF8A5100)` | `SMALLER_PROFIT` state in light mode |
| `warningDark` | `Color(0xFFFFB951)` | `SMALLER_PROFIT` state in dark mode |

These are accessed directly (not via `MaterialTheme.colorScheme`) and selected based on `isSystemInDarkTheme()`.

---

## HomeScreen

**File:** `ui/screen/home/HomeScreen.kt`
**ViewModel:** `ui/screen/home/HomeViewModel.kt`

### Purpose
Entry point of the app. Shows a welcome greeting, a card for the last store the user created (tapping it navigates to `StoreDetailScreen`), a quick "Record a sale" button attached beneath the store card, a button to navigate to the store list, and an overview of that store's key metrics.

### State

`HomeViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `lastStore` | `StateFlow<Store?>` | Most recently created store; `null` if no stores exist yet |

### Components

#### `StoreCard` (from `ui/common/StoreCard.kt`)
Used to display the last store. Accepts `Store?` and `onClick`. See [Common UI Components](#storecard-uicommonstorecard) below.

#### "Record a sale" button
Rendered directly beneath the `StoreCard` when a store exists, sharing its horizontal padding. Uses rounded corners only on the bottom (`topStart = 0.dp`, `topEnd = 0.dp`) to visually attach it to the card. Navigates to `RecordSaleScreen(storeId)` for the last store. Uses `tertiary` container colour.

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
- `LazyColumn` of `StoreCard` items (from `ui/common`) with `Modifier.animateItem()` — new stores animate in automatically when the Flow emits the updated list after creation.
- Full-width "Create new store" `Button` pinned at the bottom.
- Empty state shown when `stores` is empty.

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

## StoreDetailScreen

**File:** `ui/screen/store/StoreDetailScreen.kt`
**ViewModel:** `ui/screen/store/StoreDetailViewModel.kt`

### Purpose
Shows a single store's overview: cover photo, logo, key metrics, and analytics placeholder. Entry point to the product list and store editing.

### State

`StoreDetailViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `store` | `StateFlow<Store?>` | The store loaded by ID from `SavedStateHandle`; `null` while loading |

### Layout

#### Header (`StoreDetailHeader`)
A `Box` with total height of `photoHeight + logoRadius` (240dp = 200dp photo + 40dp overlap):

- **Cover photo**: full-width `AsyncImage` at 200dp height; `surfaceVariant` background shown when no `photoUrl` is set.
- **Logo circle**: 80dp `Box` with `CircleShape`, centered horizontally, offset so its center sits exactly at the photo's bottom edge (40dp overlap). Shows `AsyncImage` when `logoUrl` is set, otherwise `Icons.Default.Star` placeholder. 2dp `outline` border.
- **Store name** (`headlineSmall`, Bold) and **currency** (`bodyMedium`, `onSurfaceVariant`) centered in a `Column` directly below the header box.

#### Body
- Optional description text (hidden when blank).
- `OverviewCard` — placeholder metrics (Total sales, Revenue, Profit, Products in stock, Low stock alerts) showing `—`.
- `AnalyticsPlaceholderCard` — "Charts and analytics coming soon" copy.
- Full-width "Products" `Button` → navigates to `ProductListScreen`.
- Full-width "Edit store" `OutlinedButton` → navigates to `CreateStoreScreen(storeId)`.

---

## ProductListScreen

**File:** `ui/screen/product/ProductListScreen.kt`
**ViewModel:** `ui/screen/product/ProductListViewModel.kt`

### Purpose
Lists all products belonging to a store. Entry point to create or edit products.

### State

`ProductListViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `products` | `StateFlow<List<Product>>` | All products for the store, ordered by `name ASC` |

### Layout
- Title at the top.
- `LazyVerticalGrid` with `GridCells.Fixed(2)` and `Modifier.animateItem()` on each cell — products animate in/out when the list changes.
- Full-width "Add product" `Button` pinned at the bottom.
- Empty state shown when `products` is empty.

### `ProductCard` (`ProductCard.kt`)
Square card (`aspectRatio(1f)`) showing:
- `AsyncImage` filling the top portion with `ContentScale.Crop`; `Icons.Default.ShoppingCart` placeholder when `imageUrl` is blank.
- Product name (bold), selling price, and stock count below the image.

Previews: light / dark (2 total).

---

## CreateProductScreen

**File:** `ui/screen/product/CreateProductScreen.kt`
**ViewModel:** `ui/screen/product/CreateProductViewModel.kt`

### Purpose
Form to create a new product or edit an existing one within a store. Determined by the `productId` route parameter (`null` = create, non-null = edit).

### State

`CreateProductViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `formState` | `CreateProductFormState` | All form field values + validation flags + loading state |
| `visiblePermissionDialogQueue` | `SnapshotStateList<String>` | Permissions awaiting a dialog |
| `events` | `Flow<CreateProductEvent>` | One-shot events (e.g. `ProductSaved`) |

`CreateProductFormState` fields:

| Field | Default | Notes |
|---|---|---|
| `name` | `""` | Required — `nameError = true` if blank on save |
| `description` | `""` | Optional |
| `price` | `""` | Required — `priceError = true` if blank or ≤ 0 |
| `costPrice` | `""` | Optional — used for profit calculation |
| `stock` | `"0"` | Optional — empty string is treated as `0` on save |
| `currency` | `Currency.USD` | Loaded from the parent store; used as field prefix |
| `imageUrl` | `""` | Set after camera capture or gallery pick |
| `isLoading` | `false` | Disables save button and shows progress indicator |
| `nameError` | `false` | Shows supporting error text under the name field |
| `priceError` | `false` | Shows supporting error text under the price field |

The ViewModel loads the store currency **before** loading the product in a single sequential coroutine — this prevents a race condition where the product data (loaded in a separate coroutine) could reset the currency back to `USD` by creating a fresh `FormState`.

### Form fields (top to bottom)
1. Product image section — 60% width square frame centered (`ShoppingCart` placeholder or `AsyncImage`) + "Take photo" / "Choose from gallery" buttons
2. Product name (`OutlinedTextField`, required)
3. Description (`OutlinedTextField`, multiline 3–5 lines)
4. Selling price + Cost price in a side-by-side `Row` (each `weight(1f)`, decimal keyboard) — prefixed with the store's currency code (e.g. `USD`, `BOB`); auto-formats to 2 decimal places on focus loss
5. Stock (`OutlinedTextField`, number keyboard) — shows `"0"` as a placeholder; the field clears to empty when focused and `value == "0"`, preventing the user from having to delete the default value before typing
6. `LinearProgressIndicator` (visible only when `isLoading`)
7. Save `Button` (disabled while loading)

### Camera / photo flow
Single image field — no `CameraTarget` enum needed. Follows the same `cameraReadyToLaunch` + `LaunchedEffect` bridge pattern as `CreateStoreScreen`. Gallery images are copied to `filesDir` on `Dispatchers.IO`. Camera files are created directly in `filesDir`. See [permission-handling.md](permission-handling.md).

### Edit mode
When `productId != null` the ViewModel pre-populates `formState` from `productRepository.getById()`. On save, calls `productRepository.update()` instead of `create()`.

### Navigation after save
ViewModel sends `CreateProductEvent.ProductSaved` via a `Channel`. The screen pops the back stack, and `ProductListScreen`'s grid animates in the new item automatically.

---

## RecordSaleScreen

**File:** `ui/screen/sale/RecordSaleScreen.kt`
**ViewModel:** `ui/screen/sale/RecordSaleViewModel.kt`

### Purpose
Form to log a new sale against a store. Supports selecting an existing product or entering a custom (ad-hoc) product name. Tracks price/cost modifications relative to catalogue values and records the profit outcome.

### State

`RecordSaleViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `formState` | `StateFlow<RecordSaleFormState>` | All form field values, computed fields, and validation flags |

`RecordSaleFormState` fields:

| Field | Default | Notes |
|---|---|---|
| `products` | `emptyList()` | Store's product catalogue, collected from `ProductRepository` |
| `selectedProduct` | `null` | Product chosen from the dropdown |
| `isCustomProduct` | `false` | `true` when the user selects the "Custom product" option |
| `productName` | `""` | Free-text name; required when no product is selected |
| `quantity` | `"1"` | Integer; required and must be > 0 |
| `unitPrice` | `""` | Decimal; required and must be > 0 |
| `unitCost` | `""` | Decimal; optional — used for profit calculation |
| `notes` | `""` | Optional free-text |
| `soldAt` | `now` | Unix timestamp (ms) — defaults to current time |
| `currency` | `Currency.USD` | Loaded from the parent store |
| `totalAmount` | `0.0` | Computed: `unitPrice × quantity`; updated reactively |
| `isPriceModified` | `false` | `true` if `unitPrice ≠ selectedProduct.price` |
| `isCostModified` | `false` | `true` if `unitCost ≠ selectedProduct.costPrice` |
| `profitOutcome` | `NORMAL_PROFIT` | Computed by `computeProfitOutcome()` — see below |
| `profitDelta` | `0.0` | `currentProfit − standardProfit` (can be negative) |
| `currentProfit` | `0.0` | `unitPrice − unitCost` at the current field values |
| `isSaving` | `false` | Disables Save button while the coroutine runs |
| `isSuccess` | `false` | Triggers `onSaleRecorded()` navigation when `true` |

### Profit outcome logic

Computed by `computeProfitOutcome()` in the ViewModel whenever `unitPrice` or `unitCost` changes:

```
standardProfit = product.price − product.costPrice
currentProfit  = unitPrice − unitCost
delta          = currentProfit − standardProfit

LOSS           → currentProfit ≤ 0
EXTRA_PROFIT   → delta > 0
SMALLER_PROFIT → delta < 0
NORMAL_PROFIT  → delta == 0 or no modification
```

On save, `SaleType.MODIFIED` is stored when either price or cost was changed; `SaleType.STANDARD` otherwise.

### Input sanitisation

All numeric fields reject non-numeric input at the ViewModel level:
- **Decimal fields** (`unitPrice`, `unitCost`): `toDecimalInput()` strips non-digits except `.`, and caps fractional digits at 2.
- **Integer fields** (`quantity`): `toIntegerInput()` strips everything except digits.

Money fields also auto-format to `"%.2f"` on focus loss via `onFocusChanged` modifier in `SalePriceRow`.

### Form fields (top to bottom)
1. `ProductDropdown` — visible only when the store has products
2. Product name (`OutlinedTextField`) — visible when no products exist or when "Custom product" is selected
3. Quantity (`OutlinedTextField`, integer keyboard)
4. `SalePriceRow` — unit price + unit cost side-by-side, prefixed with the store's currency code
5. `SaleTotalSection` — animated total display
6. `SaleModificationInfo` — animated modification info block
7. `DateField` — date picker for the sale date
8. Notes (`OutlinedTextField`, multiline 3–5 lines)
9. Save `Button` in the bottom bar (disabled while saving)

### Child composables

#### `ProductDropdown` (`ProductDropdown.kt`)
`ExposedDropdownMenuBox` listing store products plus a "Custom product" option at the bottom. Read-only `OutlinedTextField` shows the selected item's name. Callbacks: `onProductSelected(Product?)` and `onCustomSelected()`.

Previews: light / dark (2 total).

#### `SalePriceRow` (`SalePriceRow.kt`)
Side-by-side `Row` of two `OutlinedTextField`s (unit price and unit cost), each with `weight(1f)` and `KeyboardType.Decimal`. Currency code is shown as a prefix. `onFocusChanged` formats to `"%.2f"` when focus leaves a non-empty field.

Previews: light / dark (2 total).

#### `SaleTotalSection` (`SaleTotalSection.kt`)
`AnimatedVisibility` (fade + expand) wrapping a divider/total/divider layout. Visible when `totalAmount > 0.0`. Displays `"Total: <currency> <amount>"` in `titleMedium` semibold.

Previews: light / dark (2 total).

#### `SaleModificationInfo` (`SaleModificationInfo.kt`)
`AnimatedVisibility` block shown when a product is selected and at least one price has been modified. Displays:
- A subtitle naming which fields changed (price, cost, or both)
- A body paragraph built with `buildAnnotatedString` — the profit delta value is **bold** via `SpanStyle(fontWeight = FontWeight.Bold)`

Color reflects outcome:

| `ProfitOutcome` | Color |
|---|---|
| `EXTRA_PROFIT` | `MaterialTheme.colorScheme.tertiary` |
| `SMALLER_PROFIT` | `warningLight` / `warningDark` |
| `LOSS` | `MaterialTheme.colorScheme.error` |
| `NORMAL_PROFIT` | `MaterialTheme.colorScheme.onSurface` |

Previews: light / dark × EXTRA_PROFIT / SMALLER_PROFIT / LOSS (6 total).

#### `DateField` (`DateField.kt`)
Read-only `OutlinedTextField` displaying the selected date formatted as `"MMM dd, yyyy"`. A "Change" `TextButton` in the trailing slot opens a `DatePickerDialog`. Confirming updates the epoch millis via `onDateSelected`.

Previews: light / dark (2 total).

### Navigation after save
`isSuccess = true` in `formState` is observed in a `LaunchedEffect` on the screen. When it fires, `onSaleRecorded()` is called, which pops the back stack.

---

## Common UI Components

### `StoreCard` (`ui/common/StoreCard.kt`)
Shared clickable card used in both `HomeScreen` and `StoreListScreen`. Accepts `Store?` (null = empty state) and `onClick: () -> Unit = {}`.

- **With a store:** `AsyncImage` at 180dp height with vertical gradient scrim (transparent → `surfaceContainerHigh`). Logo circle (40dp, `CircleShape`) in the top-left corner showing `logoUrl` or `Icons.Default.Star` placeholder. Currency label anchored bottom-right. Store name + optional description bottom-left.
- **Without a store:** Centered empty-state text prompting the user to create their first store.

Previews: light / dark × with store / empty state (4 total).

### `PermissionDialog` (`ui/common/PermissionDialog.kt`)
Reusable `AlertDialog` for runtime permission rationale and permanent-denial flows. See [permission-handling.md](permission-handling.md).

### `ActivityExtensions.kt` (`ui/common/ActivityExtensions.kt`)
`Activity.openAppSettings()` — navigates the user to the app's system settings page.
