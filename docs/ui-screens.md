# UI Screens

## Conventions

All screens follow the **stateful / stateless split**:

- The **stateful** composable (`fun XScreen(viewModel = hiltViewModel())`) collects state from the ViewModel and delegates to the stateless version.
- The **stateless** composable (`private fun XScreenContent(...)`) takes plain values and lambdas — fully previewable without Hilt.

Every screen has **light and dark `@Preview`** variants. Screens with a meaningful empty state have an additional empty-state preview pair.

Strings are never hardcoded in composables — all user-visible text lives in `res/values/strings.xml` and is accessed via `stringResource()` / `stringArrayResource()`.

Colors are always taken from `MaterialTheme.colorScheme`. No hardcoded color values in UI code.

### State / event class conventions

Every ViewModel exposes a single `StateFlow<XxxState>` (never raw `StateFlow<List<T>>` or `StateFlow<T?>`). Each state class and event sealed class lives in its **own file**, co-located with the ViewModel in the same feature package:

| Pattern | File | Example |
|---|---|---|
| Read-only list / detail screen | `XxxUiState.kt` | `StoreListUiState.kt` |
| Form screen | `XxxFormState.kt` | `CreateStoreFormState.kt` |
| One-shot navigation events | Defined in `XxxFormState.kt` | `sealed class CreateStoreEvent` |

State is collected in screens via `collectAsStateWithLifecycle()`. No `mutableStateOf` is used in ViewModels — all mutable state is `MutableStateFlow`.

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
| `uiState` | `StateFlow<HomeUiState>` | Driven by `UserSettingsRepository.lastAccessedStoreId` — see below |
| `isSyncing` | `StateFlow<Boolean>` | Mirrors `SyncManager.isSyncing`; used to gate auto-navigation |

`HomeUiState` fields:

| Field | Notes |
|---|---|
| `lastStore: Store?` | The last explicitly-visited store; `null` until the user visits one |
| `totalSales: Int` | Sale count for `lastStore` |
| `formattedRevenue: String` | Total revenue formatted as `"%.2f"` |
| `formattedProfit: String` | Total profit formatted as `"%.2f"` |
| `totalStock: Int` | Sum of all product stocks for `lastStore` |
| `lowStockAlerts: String?` | Comma-joined names of products with stock ≤ 5 (top 3); `null` when none |
| `hasProducts: Boolean` | `true` when the store has at least one product |
| `isInitialized: Boolean` | `false` on the initial empty value; `true` once the DataStore / Room emission arrives |

`uiState` is driven by `UserSettingsRepository.lastAccessedStoreId.flatMapLatest { … }`. If `lastAccessedStoreId` is `null` (fresh install / no store visited yet), `uiState` emits `HomeUiState(isInitialized = true)` with `lastStore = null`.

State class: `HomeUiState.kt`

### Auto-navigation

`HomeScreen` auto-navigates to `RecordSaleScreen` for the last store on first composition. The `LaunchedEffect` that triggers it has **three guards** — all must pass before navigation fires:

1. `uiState.isInitialized` must be `true` — waits for Room/DataStore to emit.
2. `isSyncing` must be `false` — waits for any in-progress initial sync to complete.
3. `viewModel.shouldAutoNav()` must return `true` — a one-shot flag; `markAutoNavHandled()` is called after navigation to prevent re-firing on recomposition.

Additionally, `val lastStore = uiState.lastStore ?: return@LaunchedEffect` acts as a fourth guard: if no store has ever been explicitly visited (`lastAccessedStoreId == null`), `lastStore` is `null` and the effect returns early without burning the one-shot flag.

The `LaunchedEffect` key is `(uiState.isInitialized, isSyncing)` so it re-runs when sync finishes and re-evaluates the guards with the final state.

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
| `uiState` | `StateFlow<StoreListUiState>` | Wraps `stores: List<Store>` — all stores, ordered by `createdAt DESC` |

State class: `StoreListUiState.kt`

### Layout
- Title at the top.
- `LazyColumn` of `StoreCard` items (from `ui/common`) with `Modifier.animateItem()` — new stores animate in automatically when the Flow emits the updated list after creation.
- Full-width "Create new store" `Button` pinned at the bottom.
- Empty state shown when `uiState.stores` is empty.

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
| `formState` | `StateFlow<CreateStoreFormState>` | All form field values + validation flags + loading state |
| `visiblePermissionDialogQueue` | `SnapshotStateList<String>` | Permissions awaiting a dialog |
| `events` | `Flow<CreateStoreEvent>` | One-shot events (e.g. `StoreSaved`) |

State + event classes: `CreateStoreFormState.kt`

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
| `uiState` | `StateFlow<StoreDetailUiState>` | Wraps `store: Store?` — loaded by ID from `SavedStateHandle`; `null` while loading |

State class: `StoreDetailUiState.kt`

### Layout

#### Header (`StoreDetailHeader`)
A `Box` with total height of `photoHeight + logoRadius` (240dp = 200dp photo + 40dp overlap):

- **Cover photo**: full-width `AsyncImage` at 200dp height; `surfaceVariant` background shown when no `photoUrl` is set.
- **Logo circle**: 80dp `Box` with `CircleShape`, centered horizontally, offset so its center sits exactly at the photo's bottom edge (40dp overlap). Shows `AsyncImage` when `logoUrl` is set, otherwise `Icons.Default.Star` placeholder. 2dp `outline` border.
- **Store name** (`headlineSmall`, Bold) and **currency** (`bodyMedium`, `onSurfaceVariant`) centered in a `Column` directly below the header box.

#### Body
- Optional description text (hidden when blank).
- Currency label.
- **"Sales"** `Button` (secondary colour) → navigates to `SalesListScreen`.
- **"Sales on Credit"** `OutlinedButton` → navigates to `CreditSalesListScreen`.
- **"Products"** `Button` (tertiary colour) → navigates to `ProductListScreen`.
- **"Inventory History"** `OutlinedButton` → navigates to `InventoryHistoryScreen`.
- `StoreDetailOverviewCard` — live metrics: total sales count, formatted revenue, formatted profit, total stock, low-stock product names.
- `StoreDetailAnalyticsCard` — "Charts and analytics coming soon" placeholder.
- **"Delete store"** `OutlinedButton` (error colour) — opens `DeleteStoreDialog` for confirmation before calling `viewModel.deleteStore()`. On deletion the screen pops via `LaunchedEffect(uiState.deleted)`.

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
| `uiState` | `StateFlow<ProductListUiState>` | Products list + stock-update dialog state |

State class: `ProductListUiState.kt`

`ProductListUiState` fields:

| Field | Default | Notes |
|---|---|---|
| `products` | `emptyList()` | All products for the store, collected from `ProductRepository` |
| `stockDialogProduct` | `null` | The product whose stock dialog is open; `null` when closed |
| `stockDialogInput` | `""` | Current text in the stock input field |
| `isSavingStock` | `false` | `true` while the update coroutine runs; disables all dialog controls |

### Layout
- Title at the top.
- `LazyVerticalGrid` with `GridCells.Fixed(2)` and `Modifier.animateItem()` on each cell — products animate in/out when the list changes.
- Full-width "Add product" `Button` pinned at the bottom.
- Empty state shown when `uiState.products` is empty.
- `UpdateStockDialog` rendered on top when `stockDialogProduct` is non-null.

### `ProductCard` (`ProductCard.kt`)
Card showing:
- Image area (`aspectRatio(1f)`): `AsyncImage` with `ContentScale.Crop`; `Icons.Default.Share` placeholder when `imageUrl` is blank. A `SmallFloatingActionButton` (using `ic_box_add`) sits at `Alignment.BottomEnd` of the image box — tapping it opens `UpdateStockDialog` for that product.
- Info area below: product name (bold, single line), selling price, and stock count.

Previews: light / dark (2 total).

### `UpdateStockDialog` (`UpdateStockDialog.kt`)
`AlertDialog` for quickly editing a product's stock without opening the full edit form. Contains:
- Product name as title.
- Current stock label (read-only, pre-set from `product.stock`).
- `OutlinedTextField` for the new stock value (number keyboard).
- `FilledTonalIconButton` row with subtract (`ic_remove`) and add (`ic_add`) buttons.
- Save button with an `AnimatedContent` fade+scale transition between the label and a `CircularProgressIndicator`.

All controls are disabled while `isSavingStock = true`. Tapping outside or the Cancel button is blocked during saving. On success the dialog is dismissed automatically by the ViewModel.

Previews: light / dark / saving state (3 total).

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
| `formState` | `StateFlow<CreateProductFormState>` | All form field values + validation flags + loading state |
| `visiblePermissionDialogQueue` | `SnapshotStateList<String>` | Permissions awaiting a dialog |
| `events` | `Flow<CreateProductEvent>` | One-shot events (e.g. `ProductSaved`) |

State + event classes: `CreateProductFormState.kt`

Focus-event methods on `CreateProductViewModel`:

| Method | Trigger | Effect |
|---|---|---|
| `onPriceFocusLost()` | Price field loses focus | Formats `price` to `"%.2f"` if non-empty and parseable |
| `onCostPriceFocusLost()` | Cost price field loses focus | Formats `costPrice` to `"%.2f"` if non-empty and parseable |
| `onStockFocused()` | Stock field gains focus | Clears `stock` when its current value is `"0"` |

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

State class: `RecordSaleFormState.kt`

`RecordSaleFormState` fields:

| Field | Default | Notes |
|---|---|---|
| `products` | `emptyList()` | Store's product catalogue, collected from `ProductRepository` |
| `selectedProduct` | `null` | Product chosen from the picker grid or search; changing this triggers auto-scroll to the quantity field |
| `isCustomProduct` | `false` | `true` when the user selects the "Custom / Other" option |
| `isSearchSelected` | `false` | `true` while the search option is active |
| `isSearchExpanded` | `false` | `true` while the full-screen search UI is shown |
| `searchQuery` | `""` | Current text in the search field |
| `filteredProducts` | `emptyList()` | Products matching the current query (debounced, 1 s) |
| `productName` | `""` | Free-text name; required when no catalogue product is selected |
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
| `isOnCredit` | `false` | `true` when the user marks this sale as on credit |
| `creditPersonName` | `""` | Required when `isOnCredit = true` — name of the customer |
| `showConfirmDialog` | `false` | Shows `SaleConfirmDialog` when `true` |
| `isSaving` | `false` | Disables Save button while the coroutine runs |
| `saleResult` | `null` | `SaleResult.Success` or `SaleResult.Error` — drives the post-save result dialog |
| `creditPersonNameError` | `false` | Shows supporting error text when credit name is required but blank |
| `canSave` | `false` | Derived: product + quantity + price valid, stock not exceeded, and credit name filled if on credit |

**Derived display fields** (pre-formatted strings, computed in the ViewModel):

| Field | Derived from | Format |
|---|---|---|
| `formattedSoldAt` | `soldAt` | `"MMM dd, yyyy"` via `SimpleDateFormat` |
| `formattedTotalAmount` | `totalAmount` | `"%.2f"` — empty string when `totalAmount == 0.0` |
| `formattedUnitPrice` | `unitPrice` | `"%.2f"` of parsed double (0.0 if blank) |
| `formattedUnitCost` | `unitCost` | `"%.2f"` of parsed double (0.0 if blank) |
| `formattedProfitDelta` | `profitDelta` | `"%.2f"` of `abs(profitDelta)` |
| `formattedAbsCurrentProfit` | `currentProfit` | `"%.2f"` of `abs(currentProfit)` |
| `showModificationInfo` | `selectedProduct`, `isPriceModified`, `isCostModified` | `true` when a product is selected and at least one price was changed |

All derived fields are recomputed by the private `withComputedDisplayFields()` extension on `RecordSaleFormState` every time any of `unitPrice`, `unitCost`, `quantity`, or `soldAt` changes. Composables receive only pre-formatted strings — no formatting logic in UI code.

Focus-event methods on `RecordSaleViewModel`:

| Method | Trigger | Effect |
|---|---|---|
| `onUnitPriceFocusLost()` | Unit price field loses focus | Formats `unitPrice` to `"%.2f"` and recomputes display fields |
| `onUnitCostFocusLost()` | Unit cost field loses focus | Formats `unitCost` to `"%.2f"` and recomputes display fields |

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

Money fields also auto-format to `"%.2f"` on focus loss. The `onFocusChanged` modifier in `SalePriceRow` calls `onUnitPriceFocusLost()` / `onUnitCostFocusLost()` on the ViewModel — all formatting happens there, not in the composable.

### Form fields (top to bottom)
1. `ProductPickerGrid` — 3-column, 3-row-tall scrollable grid; first two cells are "Search product" and "Custom / Other" action cards, remaining cells show catalogue products with their images
2. Product name (`OutlinedTextField`) — visible when no products exist or when "Custom / Other" is selected
3. `QuantityField` — tall centered number input with a pill-shaped stepper (minus left / plus right)
4. `SaleTotalSection` — animated centered total display (label above, large value below)
5. `SalePriceRow` — unit price + unit cost side-by-side, prefixed with the store's currency code
6. `SaleModificationInfo` — animated modification info block
7. `DateField` — date picker for the sale date
8. Notes (`OutlinedTextField`, multiline 3–5 lines)
9. `OnCreditSection` — checkbox labelled "On credit"; when checked a customer-name `OutlinedTextField` appears below it (required to enable Save)
10. Save `Button` in the bottom bar (disabled while saving)

### Child composables

#### `SaleFormContent` (`SaleFormContent.kt`)
Internal wrapper composable that renders the full scrollable form. Extracted from `RecordSaleScreenContent` so that the `AnimatedContent` in the screen can swap between `SaleFormContent` (form mode) and `SearchResultsContent` (search mode) with a fade transition. Receives the full `RecordSaleFormState` and all event callbacks — no business logic inside.

Uses `BoxWithConstraints` to capture the viewport height and a `rememberScrollState` reference. A `LaunchedEffect` keyed on `selectedProduct` and `isCustomProduct` fires when a product or custom option is chosen, calling `scrollState.animateScrollTo()` with a target that centres the `QuantityField` in the viewport. The field's position is tracked via `Modifier.onGloballyPositioned`.

#### `ProductPickerGrid` (`ProductPickerGrid.kt`)
3-column lazy grid rendered inside `BoxWithConstraints`. The grid height is set to `maxWidth` — because cells are square (`aspectRatio(1f)`) and gaps are equal in both directions, this makes exactly 3 rows visible at once; additional products scroll within the fixed viewport.

Items are modelled as a sealed `GridItem` interface (`Search`, `Custom`, `ProductItem`). The first two slots are always `PickerActionCard`s (icon + label, centred). Remaining slots are `PickerProductCard`s showing the product image (`AsyncImage` with `ic_package_2` fallback) and the product name centred below. When an item is selected its card switches to `primaryContainer` and a `primary.copy(alpha = 0.4f)` overlay is drawn on top of the image area.

Callbacks: `onProductSelected(Product)`, `onCustomSelected()`, `onSearchSelected()`.

Previews: light / dark (2 total).

#### `QuantityField` (`QuantityField.kt`)
Composite composable with three parts:
- **Label** — `labelMedium` text above, coloured `error` when the field is in error state.
- **`QuantityInput`** — `BasicTextField` at 72dp height with `headlineMedium` text aligned to the centre. Border colour cycles through `outline` → `primary` (focused) → `error` using `MutableInteractionSource`. Cursor brush uses `primary`.
- **`QuantitySteppers`** — 52dp `Row` of two `FilledTonalButton`s with no gap between them. The left button has large `topStart`/`bottomStart` corner radii and the right has `topEnd`/`bottomEnd`, so together they form one pill split down the middle by a 1dp `outlineVariant` divider. Left button calls `onDecrement` (disabled when `qty ≤ 1`); right calls `onIncrement` (disabled when at max stock).
- **Error text** — `bodySmall` error-coloured message shown below the steppers when `errorMessage != null`.

Previews: light / dark (2 total).

#### `SalePriceRow` (`SalePriceRow.kt`)
Side-by-side `Row` of two `OutlinedTextField`s (unit price and unit cost), each with `weight(1f)` and `KeyboardType.Decimal`. Currency code is shown as a prefix. `onFocusChanged` delegates to `onUnitPriceFocusLost` / `onUnitCostFocusLost` callbacks — no formatting logic inside the composable.

Previews: light / dark (2 total).

#### `SaleTotalSection` (`SaleTotalSection.kt`)
`AnimatedVisibility` (fade + expand) wrapping a centred column. Visible when `formattedTotalAmount` is non-empty. Layout (top to bottom):
- `labelLarge` "Total" label in `onSurfaceVariant`.
- `displaySmall` bold `"<currency> <formattedTotalAmount>"` value.
- `HorizontalDivider` in `outlineVariant`.

Appears immediately after `QuantityField`, before `SalePriceRow`, so the user sees the running total as soon as they adjust quantity.

Previews: light / dark (2 total).

#### `SaleModificationInfo` (`SaleModificationInfo.kt`)
`AnimatedVisibility` block driven by `showModificationInfo: Boolean`. When visible, displays:
- A subtitle naming which fields changed (price, cost, or both)
- A body paragraph built with `buildAnnotatedString` — the profit delta value (`formattedProfitDelta`) is **bold** via `SpanStyle(fontWeight = FontWeight.Bold)`

Accepts pre-formatted strings: `formattedUnitPrice`, `formattedUnitCost`, `formattedProfitDelta`, `formattedAbsCurrentProfit`. No formatting or `abs()` logic inside the composable.

Color reflects outcome:

| `ProfitOutcome` | Color |
|---|---|
| `EXTRA_PROFIT` | `MaterialTheme.colorScheme.tertiary` |
| `SMALLER_PROFIT` | `warningLight` / `warningDark` |
| `LOSS` | `MaterialTheme.colorScheme.error` |
| `NORMAL_PROFIT` | `MaterialTheme.colorScheme.onSurface` |

Previews: light / dark × EXTRA_PROFIT / SMALLER_PROFIT / LOSS (6 total).

#### `DateField` (`DateField.kt`)
Read-only `OutlinedTextField` displaying the selected date. Accepts `epochMillis: Long` (used to seed `rememberDatePickerState`) and `formattedDate: String` (pre-formatted by the ViewModel, displayed as-is). A "Change" `TextButton` in the trailing slot opens a `DatePickerDialog`. Confirming updates the epoch millis via `onDateSelected`. No date formatting inside the composable.

Previews: light / dark (2 total).

#### `SaleConfirmDialog` (`SaleConfirmDialog.kt`)
`AlertDialog` shown when the user taps Save, before the sale is persisted. Displays a read-only summary of the pending sale using private `ConfirmRow` composables (label in `labelSmall` / `onSurfaceVariant`, value in `bodyMedium` bold). Optional rows for: unit cost (hidden when `"0.00"`), notes (hidden when blank), and on-credit customer name (shown when `isOnCredit = true`). Confirm button calls `onConfirm`; dismiss/cancel calls `onDismiss`. All values are pre-formatted strings received from the ViewModel.

Previews: light / dark (2 total).

#### `OnCreditSection` (inside `SaleFormContent.kt`)
Private composable rendered at the bottom of `SaleFormContent`. Contains:
- A `Row` with a `Checkbox` and the label "On credit".
- When checked, an `OutlinedTextField` for the customer's name slides in below, with error state support.

`onOnCreditChange(Boolean)` and `onCreditPersonNameChange(String)` delegate directly to the ViewModel. Toggling the checkbox off clears `creditPersonName` and resets the error flag.

#### `SearchTopBar` (`SearchTopBar.kt`)
Replaces the regular `TopAppBar` while search is active. Renders as a `Surface` with a `Row` containing an `OutlinedTextField` (search query, `ImeAction.Search`) and a "Close" `Button`. The text field holds a `FocusRequester` so the screen can request keyboard focus via `LaunchedEffect` as soon as search expands. Both the IME Search action and the Close button call `onSearchConfirmed()` and hide the software keyboard.

Previews: 1 (light only).

#### `SearchResultsContent` (`SearchResultsContent.kt`)
Replaces `SaleFormContent` in the content area while search is active. Scrollable `Column` of `TextButton` rows — each row spans the full width with the product name on the left and price on the right. Tapping a row calls `onProductSelected(product)`, which collapses the search and pre-fills the form. Shows a "no results" message when the query is non-blank and `filteredProducts` is empty; shows nothing when the query is blank and no results exist.

Previews: results list / empty state (2 total, light only).

### Navigation after save
A `SaleResultDialog` is shown after the save coroutine completes. On dismissal, if the result was `SaleResult.Success`, `onSaleRecorded()` is called and the back stack is popped.

---

## SalesListScreen

**File:** `ui/screen/sale/SalesListScreen.kt`
**ViewModel:** `ui/screen/sale/SalesListViewModel.kt`

### Purpose
Paginated, filterable list of all sales for a store. Shows all sales regardless of credit status. Credit sales are visually labelled. Entry point to sale detail and the sales report.

### State

`SalesListViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `uiState` | `StateFlow<SalesListUiState>` | Filtered/paged sales, filter values, product list |

`SalesListUiState` fields:

| Field | Notes |
|---|---|
| `sales: List<SaleDisplayItem>` | Current page of display items |
| `products: List<Product>` | Store's product catalogue — drives the product filter dropdown |
| `selectedProduct: Product?` | Currently active product filter; `null` = all products |
| `fromDate / toDate: Long` | Active date range (epoch ms) |
| `formattedFromDate / formattedToDate: String` | Pre-formatted for the date filter chips |
| `isLoading: Boolean` | Shows `CircularProgressIndicator` while first emit arrives |
| `hasMore: Boolean` | `true` when more results exist beyond the current page |

`SaleDisplayItem` fields:

| Field | Notes |
|---|---|
| `id: Long` | Used as the `LazyColumn` item key |
| `productName: String` | |
| `formattedDate: String` | `"MMM dd, yyyy - h:mm a"` |
| `formattedTotal: String` | `"%.2f"` of `totalAmount` |
| `formattedQuantity: String` | `"×N"` |
| `isOnCredit: Boolean` | Drives the `CreditBadge` chip next to the product name |

### Layout
- `ProductDropdown` filter (hidden when no products exist).
- `DateRangeFilter` — two date chips for from/to.
- `LazyColumn` of `SaleListItem` cards with `Modifier.animateItem()`.
- Pagination footer: "Load more" `Button` when `hasMore = true`; "End of entries" label otherwise.
- Empty and loading states.

### Credit badge
`CreditBadge` is a private composable that renders the text "Credit" in a small rounded-corner chip using `secondaryContainer` / `onSecondaryContainer` colours. It appears to the right of the product name inside `SaleListItem` when `isOnCredit = true`.

### Filtering and pagination
- Page size: 15 items. `loadMore()` increments the displayed count by 15.
- Any filter change (date or product) resets `displayedCount` to 15.
- The three-way `combine` (sales flow + products flow + filter state flow) means the list reactively updates whenever a sale is added, updated, or deleted elsewhere.

### Overflow menu
A `MoreVert` icon in the top bar exposes three actions:
- **Reset filters** — resets dates to today and clears the product filter.
- **Get today's sales report** — navigates to `SalesReportScreen` with today's full-day range and no product filter.
- **Get sales report** — navigates to `SalesReportScreen` with the current active filter values.

---

## SalesReportScreen

**File:** `ui/screen/sale/SalesReportScreen.kt`
**ViewModel:** `ui/screen/sale/SalesReportViewModel.kt`

### Purpose
Aggregated analytics for a fixed date range (and optional product filter) passed in via route arguments. Read-only — no user input.

### State

`SalesReportViewModel` receives `storeId`, `fromDate`, `toDate`, and `productId?` from `SavedStateHandle`. It combines `saleRepository.getByStore()` and `productRepository.getByStore()` into a single `StateFlow<SalesReportUiState>`.

`SalesReportUiState` fields:

| Field | Notes |
|---|---|
| `formattedTotalRevenue: String` | Sum of `totalAmount` for all filtered sales |
| `formattedTotalProfit: String` | Sum of `(unitPrice − unitCost) × quantity` |
| `creditSalesCount: Int` | Number of sales where `onCredit = true` within the filtered set |
| `formattedCreditRevenue: String` | `totalAmount` sum for on-credit sales only |
| `formattedCreditProfit: String` | Profit sum for on-credit sales only |
| `inventoryItems: List<InventoryBarItem>` | Per-product sold-unit counts with a bar fraction |
| `dailyRevenue: List<DailyRevenueItem>` | Revenue by day across the selected range |
| `profitOutcomeBreakdown: List<ProfitOutcomeBreakdownItem>` | Distribution of `NORMAL / EXTRA / SMALLER / LOSS` outcomes |
| `hasData: Boolean` | `false` → shows empty-state message |

### On-credit note
When `creditSalesCount > 0`, a `CreditSalesNote` composable is rendered below the total profit figure in `TotalsSection`. It shows (in `tertiary` colour):

> _N on credit · revenue X · profit Y pending_

This indicates that the reported totals include on-credit amounts that have not yet been collected.

---

## CreditSalesListScreen

**File:** `ui/screen/sale/CreditSalesListScreen.kt`
**ViewModel:** `ui/screen/sale/CreditSalesListViewModel.kt`

### Purpose
Shows only sales where `onCredit = true` for a store, in a 2-column grid filtered by date. Tapping a card opens a confirmation dialog to mark the sale as paid, which flips `onCredit = false` via `SaleRepository.update()` and removes it from the grid automatically (the Flow re-emits).

### State

`CreditSalesListViewModel` exposes:

| Property | Type | Description |
|---|---|---|
| `uiState` | `StateFlow<CreditSalesListUiState>` | Filtered credit sales, date range, loading flag |

`CreditSalesListUiState` fields:

| Field | Notes |
|---|---|
| `sales: List<CreditSaleDisplayItem>` | Sales matching the active date filter |
| `fromDate / toDate: Long` | Active date range (epoch ms) |
| `formattedFromDate / formattedToDate: String` | Pre-formatted for the date filter chips |
| `isLoading: Boolean` | Shows `CircularProgressIndicator` on first load |

`CreditSaleDisplayItem` fields:

| Field | Notes |
|---|---|
| `id: Long` | Used as the `LazyVerticalGrid` item key |
| `productName: String` | |
| `creditPersonName: String` | Displayed as "Customer: …" below the product name |
| `formattedDate: String` | `"MMM dd, yyyy - h:mm a"` |
| `formattedTotal: String` | `"%.2f"` of `totalAmount` |
| `quantity: Int` | Displayed as `×N` |

### Default date range
On first load `fromDate` defaults to the start of the current calendar year, so all yearly credit sales are visible without the user having to adjust the filter.

### Layout
- `DateRangeFilter` at the top.
- `LazyVerticalGrid` with `GridCells.Fixed(2)` — `CreditSaleGridItem` cards.
- Empty state includes a "Reset filters" `Button` to return to the default date range.
- Loading state shows a centred `CircularProgressIndicator`.

### `CreditSaleGridItem` (`CreditSaleGridItem.kt`)
Card composable for the grid. Shows product name (bold, up to 2 lines), customer name in `secondary` colour, total amount, quantity, and date. Tapping calls `onClick`.

Previews: light / dark (2 total).

### Mark as paid
When the user taps a grid item, `pendingDoneItem` state is set and a `MarkAsDoneDialog` appears:

> _"Mark as paid? This will mark [customer]'s sale as collected and remove it from this list."_

On confirm, `viewModel.markAsDone(id)` is called, which fetches the sale by ID and calls `saleRepository.update(sale.copy(onCredit = false))`. The `getOnCreditByStore` Flow re-emits the shorter list and the item disappears from the grid.

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
