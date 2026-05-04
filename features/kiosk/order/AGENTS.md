# Kiosk Order Feature

## Business Context
The kiosk order screen is the post-identify menu surface — what guests see after a successful identify or skip. It mirrors the consumer order screen's domain layer (`features/shared/menu/api/domain.GetOrderContent`) but renders kiosk-tuned UI: vertical Material `NavigationRail` of categories on the left, 3-column item grid in the middle, and a cart bar with cart total / Cancel / Pay actions at the bottom. Matches Image 3 (Burgers grid + sidebar) and Image 4 (Around the World Menu + bottom strip).

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| KioskOrderScreen | api/navigation | `data object`, plain `Screen`. Reached via `IdentifyScreen(next = KioskOrderScreen)` after successful identify or skip. |
| KioskOrderTestTags | api/navigation (`api/ui` subpackage) | Constants for nav rail, top bar pills, item grid, item cards, cart bar, cart total, Cancel/Pay buttons, Back/Scan-Offer buttons. |
| KioskOrderPresenter | impl/presentation | `@CircuitInject(KioskOrderScreen)`. Reuses `GetOrderContent` from `features/shared/menu/api/domain`. Tracks `selectedCategoryId` locally; computes `itemsForSelectedCategory` from `OrderContent.itemsByCategory[selected]`. |
| KioskOrderUiState | impl/presentation | `data class(categories, selectedCategoryId, itemsForSelectedCategory, cartSummary, eventSink)` + computed `selectedCategoryName`. |
| KioskOrderEvent | impl/presentation | sealed: `CategorySelected(id)`, `ItemTapped(id)`, `ScanOfferPressed`, `BackPressed`, `CartPressed`, `CancelOrderPressed`, `PayPressed`. |
| KioskOrderUi | impl/presentation/androidMain | Row layout: `NavigationRail` (left, 200dp wide, HOME tile + per-category items) + Column (top bar + item grid + cart bar). Kiosk-scaled dimensions (item cards 360dp tall, cart bar 140dp, cart buttons 96dp tall). |
| FakeKioskOrderContent | test | Object with `DEFAULT_CATEGORIES` / `DEFAULT_ITEMS` / `DEFAULT_CART` / `DEFAULT_CONTENT` builders. The actual `GetOrderContent` Fake lives in `features/shared/menu/test/` and is the binding both consumer and kiosk navint tests use. |

## Cross-Feature Dependencies
- Imports from `features/shared/menu/api/domain` — `GetOrderContent`, `OrderContent`, `MenuCategory`, `MenuItem`, `CartSummary`.
- Imports from `features/kiosk/attract/api/navigation` — `AttractScreen` (BackPressed and CancelOrderPressed both `resetRoot(AttractScreen)`).
- Imported by: `kioskComposeApp` (auto-discovered) and `features/kiosk/attract/impl/presentation` once Attract starts passing `IdentifyScreen(next = KioskOrderScreen)`.
- Konsist enforces: must not import any consumer-only feature.

## Feature-Specific Patterns
- **Reuse without duplication**: kiosk doesn't duplicate the menu domain — it consumes the existing `GetOrderContent` interactor unchanged. The only schema work was extending `OrderContent` (api/domain) additively with `MenuItem` + `itemsByCategory`. Both consumer and kiosk benefit from richer menu data when their respective UIs use it.
- **`OrderRepositoryImpl` populates kiosk-relevant categories**: Burgers, Happy Meals, Sandwiches & Meals, Main Menu, All Day Breakfast, Fries & Sides, Sweets & Treats, Beverages. Each gets ~3-10 items with calorie counts, prices, and `https://example.test/` sentinel image URLs (RFC2606 reserved domain — placeholder dev hygiene). Real backend integration replaces this entire impl in a follow-up.
- **`Back` and `Cancel` collapse to the same action v1**: both `KioskOrderEvent.BackPressed` and `CancelOrderPressed` call `navigator.resetRoot(AttractScreen)` — there's no parent stack post-identify; both buttons mean "abandon and reset." Once item-detail / cart-review sub-screens land, Back becomes intra-order navigation while Cancel stays session-reset.
- **Item-detail / cart-review / payment / receipt screens are out of scope v1**: `ItemTapped`, `ScanOfferPressed`, `CartPressed`, `PayPressed` all no-op in the presenter. Each becomes a downstream screen in a follow-up; the event surface is stable so wiring is purely additive.
- **Pay button disables when cart is empty**: `enabled = itemCount > 0` on the Pay action (greys out at alpha 0.4 with disabled text alpha 0.5).
- **Kiosk-scaled dimensions**: nav rail 200dp wide, rail items 120dp tall, item cards 360dp tall, cart bar 140dp, cart action buttons 96dp × 200dp. All sized for standing-distance touch on a vertical 32" screen.

## Testing
- Unit (presenter): `impl/presentation/src/commonTest/...` — verify state→UI plumbing, `CategorySelected` updates `selectedCategoryId`, `BackPressed`/`CancelOrderPressed` trigger `resetRoot(AttractScreen)`.
- The `GetOrderContent` Fake from `features/shared/menu/test/` drives navint tests; kiosk-specific kiosk-order navint flow assertions live in `:testing:kiosk:navint-tests` (Phase: kiosk test infra).
- E2E journey: Attract → Identify (skip) → KioskOrder → Back → Attract — covered by `:testing:kiosk:e2e-tests`.
