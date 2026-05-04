# Order Feature

## Business Context
The order screen is the primary food ordering interface, displaying menu categories, featured items with pricing, and a cart summary. Users can browse categories, add items to their order, and proceed to checkout via the cart.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| OrderScreen | api/navigation | TabScreen (data object), tag="order" |
| OrderContent | api/domain | categories, featuredItems, cartSummary |
| MenuCategory | api/domain | id, name |
| FeaturedItem | api/domain | id, title, price, description, imageUrl, tag, isPrimary |
| CartSummary | api/domain | itemCount, total |
| GetOrderContent | api/domain -> impl/domain | Streaming use case via CenterPostSubjectInteractor<Unit, OrderContent> |
| OrderRepository | impl/domain -> impl/data | getMenuCategories(), getFeaturedItems(), getCartSummary() -- all Flow-based |
| OrderPresenter | impl/presentation | Manages content streaming and local selectedCategoryId state |
| OrderUiState | impl/presentation | categories, selectedCategoryId?, featuredItems, cartSummary?, eventSink |
| OrderEvent | impl/presentation | CategorySelected(id), AddToOrder(itemId), CartClicked |

## Cross-Feature Dependencies
- Navigates to: none (AddToOrder and CartClicked are currently no-op centerPost placeholders)
- Imported by: composeApp (wired at app level)
- Core deps: core:centerpost, core:theme

## Feature-Specific Patterns
- OrderScreen is a TabScreen with tag="order", making it a bottom navigation destination.
- The presenter maintains local `selectedCategoryId` state via `remember { mutableStateOf }`, defaulting to the first category when null.
- This is the most complex domain model with three distinct entity types (categories, featured items, cart).
- FeaturedItem has `isPrimary` and `tag` fields for visual differentiation in the UI (e.g., highlighting promoted items).
- The `effectiveSelectedId` pattern falls back to the first category ID when no explicit selection has been made.

## Testing
- Unit: impl/domain/src/commonTest/ (GetOrderContentImplTest)
- Data: impl/data/src/commonTest/ (OrderRepositoryImplTest)
- Presenter: impl/presentation/src/commonTest/ (OrderPresenterTest)
- UI: impl/presentation/src/androidDeviceTest/ (OrderUiTest, OrderUiRobot, OrderStateRobot -- Robot pattern)
- Fakes: test/src/commonMain/ (FakeGetOrderContent)
