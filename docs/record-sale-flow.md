# Record Sale Flow

End-to-end walkthrough of the sale recording feature, covering initialization, product selection modes, form validation, the confirm–save cycle, and the domain use case.

---

## Entry Point

`RecordSaleScreen` is reached from:
- `HomeScreen` → "Record a sale" button (uses the last store's ID)
- `StoreDetailScreen` → "Create sale" button

Route: `RecordSaleScreen(storeId: Long)`. The `storeId` is extracted from `SavedStateHandle` inside `RecordSaleViewModel`.

---

## Initialization

On `init`, the ViewModel launches a single sequential coroutine:

1. `storeRepository.markAccessed(storeId)` — updates the store's `lastAccessedAt` timestamp in Room.
2. `userSettingsRepository.setLastAccessedStore(storeId)` — persists the store ID to DataStore Preferences. This is what enables `HomeScreen` auto-navigation: `HomeViewModel` reads this value and only populates `lastStore` once a store has been explicitly visited here or in `StoreDetailScreen`.
3. `storeRepository.getById(storeId)` — loads the store's currency and applies it to `formState.currency`.
4. `productRepository.getByStore(storeId).collect { … }` — starts collecting the product catalogue as a Flow. The list is stored in `formState.products` and drives the `ProductPickerGrid`.

Both steps happen in the same coroutine, ensuring currency is always set before product data arrives.

---

## Product Selection Modes

There are three mutually exclusive ways to choose a product. Switching between them resets all price, cost, and profit fields.

### 1. Product Picker Grid

When the store has products, a `ProductPickerGrid` is shown at the top of the form. It renders a fixed 3-column, 3-row-tall scrollable grid. The first two cells are always the "Search product" and "Custom / Other" action cards; remaining cells show catalogue products with their images and names.

- **Catalogue product selected** (`onProductSelected(product)`): pre-fills `unitPrice`, `unitCost`, `productName`. `isPriceModified` and `isCostModified` are reset to `false`. The form auto-scrolls so the quantity field is vertically centred in the viewport.
- **"Custom / Other" selected** (`onCustomProductSelected()`): clears all price fields and shows the free-text product name field. The form auto-scrolls to the quantity field in the same way.

### 2. Search

Tapping "Search product" calls `onSearchSelected()`, which sets `isSearchExpanded = true`. This triggers a dual `AnimatedContent` transition:

- **Top bar**: `TopAppBar` slides out upward, `SearchTopBar` slides in from above (slide + fade).
- **Content area**: `SaleFormContent` fades out, `SearchResultsContent` fades in.

The `SearchTopBar` immediately requests keyboard focus via `FocusRequester` in a `LaunchedEffect`.

**Debounced filtering**: `onSearchQueryChange(query)` updates the query immediately (for UI responsiveness) and cancels any previous `searchJob`, then launches a new coroutine that waits 1 second before filtering `formState.products` by name (case-insensitive). An empty query resets the list to all products.

Tapping a result in `SearchResultsContent` calls `onProductSelected(product)`, which collapses the search UI and pre-fills the form exactly like the dropdown path.

Tapping "Close" (or pressing the Search IME action) calls `onSearchConfirmed()`, which collapses the search without selecting a product.

### 3. No products in store

When `formState.products` is empty the dropdown is hidden entirely and the free-text product name field is always visible. The user types the product name directly.

---

## Form Fields and Validation

All numeric input is sanitised at the ViewModel level before being stored in `formState` — no raw strings are trusted in the UI.

| Field | Sanitiser | Rule |
|---|---|---|
| `unitPrice`, `unitCost` | `toDecimalInput()` | Strips non-digits except `.`; caps at 2 decimal places |
| `quantity` | `toIntegerInput()` | Strips all non-digit characters |

Both money fields auto-format to `"%.2f"` when the field loses focus (`onUnitPriceFocusLost` / `onUnitCostFocusLost`).

### `canSave` computation

Recomputed by `withComputedDisplayFields()` on every state change:

```
canSave = (selectedProduct != null OR productName is not blank)
       AND quantity > 0
       AND unitPrice > 0
       AND quantity does NOT exceed selectedProduct.stock
       AND (isOnCredit = false OR creditPersonName is not blank)
```

The Save button in the bottom bar is disabled when `canSave = false`.

### Stock validation

`quantityExceedsStock` is set to `true` when a catalogue product is selected and the entered quantity exceeds `selectedProduct.stock`. This shows an inline error on the quantity field and blocks saving.

### Price/cost modification tracking

When a catalogue product is selected, any change to `unitPrice` or `unitCost` is compared against the product's catalogue values. `isPriceModified` / `isCostModified` are set accordingly, and `computeProfitOutcome()` recalculates the profit outcome in real time. See [ui-screens.md](ui-screens.md#profit-outcome-logic) for the full outcome table.

---

## On Credit

The user can mark a sale as on credit by checking the "On credit" checkbox at the bottom of the form. When checked:

- `isOnCredit` is set to `true`.
- A "Customer name" `OutlinedTextField` appears, and `canSave` requires a non-blank value in this field.
- The credit person name is stored in `creditPersonName`.

On save, `onCredit = true` and `creditPersonName` are passed into the `Sale` domain object. Stock is still decremented — credit only affects payment status, not inventory.

When the sale is paid later, `CreditSalesListViewModel.markAsDone()` flips `onCredit = false` via `saleRepository.update()`.

---

## Confirm Dialog

Tapping Save does **not** immediately persist the sale. Instead:

1. `onSaveClicked()` sets `showConfirmDialog = true`.
2. `RecordSaleScreen` renders `SaleConfirmDialog` on top of the form.
3. The dialog shows a read-only summary (product name, quantity, unit price, unit cost if non-zero, total, date, notes if non-blank, and customer name if on credit).
4. **Confirm** → `confirmSave()` is called.
5. **Cancel / dismiss** → `onDismissConfirmDialog()` sets `showConfirmDialog = false` and returns the user to the form.

---

## Save Path

`confirmSave()` runs in `viewModelScope`:

```
showConfirmDialog = false
isSaving = true
    ↓
RecordSaleUseCase(sale, selectedProduct, isCustomProduct)
    ├─ saleRepository.create(sale)            → inserts SaleEntity into Room
    │      (includes onCredit and creditPersonName fields)
    ├─ if selectedProduct != null:
    │      productRepository.update(
    │          product.copy(stock = (product.stock - quantity).coerceAtLeast(0))
    │      )                                  → decrements stock (floor 0)
    └─ else if isCustomProduct and productName is not blank:
           productRepository.create(Product(…)) → creates a new catalogue entry
    ↓
isSaving = false
saleResult = SaleResult.Success(…) or SaleResult.Error
```

`SaleType` stored in the record:
- `STANDARD` — neither price nor cost was modified
- `MODIFIED` — at least one of price or cost differs from the catalogue value

`profitOutcome` (`NORMAL_PROFIT`, `EXTRA_PROFIT`, `SMALLER_PROFIT`, `LOSS`) is also persisted in the sale record for analytics purposes.

`onCredit` and `creditPersonName` are stored verbatim from `formState`. Stock is always decremented regardless of credit status.

While `isSaving = true` the Save button shows a `CircularProgressIndicator` (via `LoadingButton`) and is disabled.

---

## Navigation After Save

After `confirmSave()` completes, `saleResult` is set to `SaleResult.Success` or `SaleResult.Error`. `RecordSaleScreen` renders `SaleResultDialog` showing the outcome. When the dialog is dismissed:
- On success → `onSaleRecorded()` is called → `navController.popBackStack()`. The previous screen becomes visible.
- On error → the dialog closes, the user remains on the form to retry.

---

## State Flags Summary

| Flag | Set to `true` | Reset to `false` |
|---|---|---|
| `isSearchExpanded` | `onSearchSelected()` | `onProductSelected()`, `onCustomProductSelected()`, `onSearchConfirmed()` |
| `showConfirmDialog` | `onSaveClicked()` | `onDismissConfirmDialog()`, start of `confirmSave()` |
| `isSaving` | Start of `confirmSave()` coroutine | After `RecordSaleUseCase` completes |
| `isPriceModified` | `onUnitPriceChange()` when price ≠ catalogue | `onProductSelected()`, `onCustomProductSelected()`, `onSearchSelected()` |
| `isCostModified` | `onUnitCostChange()` when cost ≠ catalogue | `onProductSelected()`, `onCustomProductSelected()`, `onSearchSelected()` |
| `isOnCredit` | `onOnCreditChange(true)` | `onOnCreditChange(false)` |
| `creditPersonNameError` | Would be set on save validation | `onCreditPersonNameChange()`, `onOnCreditChange(false)` |
