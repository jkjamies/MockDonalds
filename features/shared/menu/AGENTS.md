# Shared Menu Feature

## Business Context
The shared menu domain is the single source of menu data consumed by both the consumer mobile app's order screen (`features/mobile/order`) and the kiosk app's order screen (`features/kiosk/order`). It provides the categories, items, featured items, cart summary, the `GetOrderContent` use case, and the `OrderRepository` that backs them. Each app renders its own screen + presenter + UI on top of this shared domain.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| OrderContent | api/domain | Top-level menu payload: `categories`, `featuredItems`, `itemsByCategory`, `cartSummary`. |
| MenuCategory | api/domain | `id`, `name`, optional `iconUrl`. |
| MenuItem | api/domain | Per-category item — `id`, `categoryId`, `name`, `priceFormatted`, `calories?`, `imageUrl`, `description?`, `tags`. |
| FeaturedItem | api/domain | Hero/banner-style item used by the consumer order screen. |
| CartSummary | api/domain | `itemCount`, `total`. |
| GetOrderContent | api/domain → impl/domain | `CenterPostSubjectInteractor<Unit, OrderContent>`. |
| GetOrderContentImpl | impl/domain | Combines `OrderRepository` flows into `OrderContent`. |
| OrderRepository | impl/domain → impl/data | Read-only Flow methods (`getMenuCategories`, `getFeaturedItems`, `getItemsByCategory`, `getCartSummary`). |
| OrderRepositoryImpl | impl/data | Static fake data v1 — 8 categories with ~30 items, sentinel `https://example.test/` image URLs. Real backend integration replaces this without breaking presenter contracts. |
| FakeGetOrderContent | test | `@ContributesBinding(AppScope::class)`. Both consumer and kiosk navint suites consume this fake. |

## Submodules
Domain-only shape — `api/domain`, `impl/data`, `impl/domain`, `test`. No `api/navigation` or `impl/presentation`: the screens live in each consuming app's feature (`mobile/order` has `OrderScreen` + `OrderPresenter`; `kiosk/order` has `KioskOrderScreen` + `KioskOrderPresenter`). Both presenters consume `GetOrderContent` from this module.

## Cross-Feature Dependencies
- Imported by: `features/mobile/order/impl/presentation` and `features/kiosk/order/impl/presentation` (presenters consume `GetOrderContent`); both apps' navint suites import `features/shared/menu/test` for `FakeGetOrderContent`.
- Imports from: `core:centerpost`, `core:network:api`, `core:build-config:api`. Konsist enforces no imports from `features.mobile.*` or `features.kiosk.*` (would pin shared to one app's specifics).

## Feature-Specific Patterns
- **Single-source menu data** — both apps see the same `OrderContent` shape. If kiosk needs a richer field that mobile doesn't render, add it additively to `OrderContent` and let the consumer presenter ignore it.
- **Class names retain `Order*` prefix** — `OrderContent`, `OrderRepository`, etc. The package path makes the shared nature clear (`com.mockdonalds.app.features.shared.menu.*`); renaming to `Menu*` is a separate concern that would touch both apps.
- **Fakes live in `features/shared/menu/test`** — both navint suites consume the same fake, ensuring identical test fixtures across the two apps.

## Future Growth
When the real menu backend integration lands, expand `OrderRepository` with real `RemoteDataSource` + DTO + mapper following the standard pattern in `.agents/standards/architecture.md`. The interface stays stable; presenters and screens don't change.
