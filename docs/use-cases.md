# Use Cases

Use cases live in `domain/usecase/` and are the single place where business logic is orchestrated. See the `/identifying-use-cases` skill for the rules that decide whether something needs a use case.

## Existing

| Use Case | What it does |
|---|---|
| `RecordSaleUseCase` | Coordinates `SaleRepository` + `ProductRepository`; auto-creates product when custom; decrements stock |
| `GetSalesHistoryUseCase` | Filters + paginates sales across `SaleRepository` + `ProductRepository`; owns date-range and product-filter rules and the `hasMore` boundary |
| `GetSalesSummaryUseCase` | Aggregates filtered sales into revenue, profit, credit totals, daily revenue map, product units sold, and profit-outcome breakdown |
| `RestockProductUseCase` | Coordinates `ProductRepository` + `InventoryLogRepository`; enforces stock ≥ 0 and no-op when unchanged; writes log entry as side effect |

## Pending

### Sales

| Use Case | Why it qualifies |
|---|---|
| `DeleteSaleUseCase` | Must reverse stock if the sale had a tracked product — cascading side effect across two repositories |

### Products

| Use Case | Why it qualifies |
|---|---|
| `GetLowStockProductsUseCase` | Enforces stock-threshold domain rule (what "low" means is a business decision, not UI) |

---

## Use cases that were ruled out

These features were considered but do **not** need a use case under the project rules:

| Feature | Reason |
|---|---|
| `GetProductsUseCase` | Plain single-repository read with no logic — ViewModel calls `ProductRepository` directly |
| `GetStoresUseCase` | Same — plain `StoreRepository.getAll()` pass-through |
| `AddProductUseCase` / `UpdateProductUseCase` | Single repository write with no cross-repo side effects or domain rules beyond basic validation |
| `GetDailyReportUseCase` | Covered by `GetSalesSummaryUseCase` with a one-day date range |
