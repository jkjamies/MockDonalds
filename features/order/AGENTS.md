# Order Feature

## Business Context
The order screen is the primary food ordering entry point. The landing screen shows a vertical list of category cards (Featured, Burgers, Chicken, Sides, Drinks, Desserts), each with a preview image pulled from the first item in that category. Tapping a card opens a category detail screen showing up to 20 menu items for that category. Items come from the Spoonacular `/food/menuItems/search` API and are cached in SQLDelight with a 24h TTL so the free-tier daily quota is not the per-screen limiter. The cart summary remains hardcoded for now — real cart wiring is a separate spec.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| OrderScreen | api/navigation | TabScreen (data object), tag="order" |
| CategoryDetailScreen | api/navigation | Regular Screen (push destination), `data class(categoryId: String)` |
| OrderContent | api/domain | categoryPreviews, cartSummary |
| MenuItem | api/domain | id, title, restaurantChain, imageUrl, servingSize?, categoryId |
| CategoryPreview | api/domain | id, name, firstItemImageUrl?, itemCount |
| CategoryDetailContent | api/domain | categoryId, categoryName, items |
| CartSummary | api/domain | itemCount, total |
| GetOrderContent | api/domain → impl/domain | Streaming `CenterPostSubjectInteractor<Unit, OrderContent>` |
| GetCategoryDetailContent | api/domain → impl/domain | Streaming `CenterPostSubjectInteractor<String, CategoryDetailContent>` (param = categoryId) |
| OrderRepository | impl/domain → impl/data | getCategoryPreviews(), getMenuItemsByCategory(id), categoryName(id), getCartSummary() |
| MenuRemoteDataSource | impl/data/remote/ | Interface; impl `KtorMenuRemoteDataSource` calls Spoonacular `/food/menuItems/search` |
| MenuItemLocalDataSource | impl/data/local/ | Interface; impl `SqlDelightMenuItemLocalDataSource` wraps generated `MenuItemQueries` (cachedMenuItem table) |
| SpoonacularHttpClient | impl/data | `@Qualifier` annotation that disambiguates the Spoonacular-scoped HttpClient |
| OrderPresenter | impl/presentation | Streams OrderContent, navigates to CategoryDetailScreen on `CategoryTapped` |
| OrderUiState | impl/presentation | categoryPreviews, cartSummary?, eventSink |
| OrderEvent | impl/presentation | CategoryTapped(id), CartClicked |
| CategoryDetailPresenter | impl/presentation | Drives the per-category list; consumes GetCategoryDetailContent + GetOrderContent (for cart) |
| CategoryDetailUiState | impl/presentation | categoryId, categoryName, items, cartSummary?, eventSink |
| CategoryDetailEvent | impl/presentation | BackPressed, AddToOrder(itemId), CartClicked |

## Cross-Feature Dependencies
- **Navigates to**: `CategoryDetailScreen` (within the same feature, as a push destination from the order tab); `AddToOrder` and `CartClicked` are no-op `centerPost` placeholders.
- **Imported by**: composeApp (wired at app level)
- **Core deps**: core:centerpost, core:network:api (HTTP), core:build-config:api (Spoonacular API key), core:logger:api (auto-wired), core:theme, core:circuit
- **Persistence**: `cachedMenuItem` table contributed via SQLDelight aggregator pattern. composeApp owns the merged `AppDatabase` and provides `MenuItemQueries` to this feature's data layer through Metro DI.

## Feature-Specific Patterns

- **Spoonacular HttpClient is feature-owned**: `SpoonacularHttpClientProvider` (Metro `@ContributesTo`) constructs a per-feature `HttpClient` via `HttpClientFactory` with `baseUrl = https://api.spoonacular.com/food/` (trailing slash is required — RFC 3986 URL resolution otherwise strips `/food` when appending a relative endpoint path), `authMode = AuthMode.NONE`, and the `x-api-key` header set to `AppBuildConfig.spoonacularApiKey`. The provider fails fast with a `require()` on a blank key so missing local.properties wiring surfaces immediately on first DI resolution rather than as silent 401s. This client is qualifier-scoped (`@SpoonacularHttpClient`) so it cannot leak into other features.
- **Akamai exception**: Spoonacular is a third-party reference integration and does **not** route through Akamai. The "Akamai required in all envs" project rule applies to first-party MockDonalds backend services only. Future networked features that consume first-party APIs must route through Akamai.
- **Cache-first read with 24h TTL**: `OrderRepositoryImpl` checks `oldestCachedAtByCategory` against `Clock.System.now() - 24h`. Stale or empty triggers a remote fetch that replaces the SQLDelight rows; cache flow re-emits via `merge` so the UI shows stale data immediately and updates in place when the refresh lands.
- **Static category list lives in the repository**: `(id, displayName, query)` tuples hardcoded in `OrderRepositoryImpl`. The taxonomy is stable for v1 and not exposed via a remote source.
- **Spoonacular API key is sourced from `local.properties`** (gitignored), not committed defaults. Empty default in `BuildConfig.SPOONACULAR_API_KEY` so the public reference repo still builds without a key. See `core/build-config/impl/build.gradle.kts` for the wiring + enterprise migration comment.
- **CategoryDetailPresenter pulls cart from `GetOrderContent`** (a small leak across screens) rather than introducing a separate use case for the hardcoded cart summary. When real cart wiring lands this should split into a dedicated `GetCartSummary` use case.
- **Data source impl naming**: data source impls use a descriptive prefix (`Ktor*`, `SqlDelight*`) not the `*Impl` suffix. Konsist's `NamingConventionsTest` enforces that only `*RepositoryImpl` classes can end in `Impl` within `impl/data/`. The descriptive prefix also documents the underlying tech.
- **No data-source `Fake*` classes**: Konsist forbids fakes in `commonTest`, and `test/` modules can't depend on `:impl:data` (per `TestModuleDependencyTest`), so data-source test doubles must be inline anonymous-object literals or private helper classes whose names avoid Konsist's `Fake/Stub/Mock/TestData/Fixture/Factory` blocklist (see `OrderRepositoryImplTest.RepositoryHarness` for the pattern).

## Testing
- Unit (commonTest):
  - `OrderRepositoryImplTest` — cache hit / stale / miss / remote failure / preview shape (host)
  - `SpoonacularMenuItemDtoMapperTest` — happy path + null serving size
  - `GetOrderContentImplTest` — combines previews + cart
  - `GetCategoryDetailContentImplTest` — name resolution + items pass-through
  - `OrderPresenterTest`, `CategoryDetailPresenterTest` — Circuit `presenterTestOf`
- UI Component (androidDeviceTest):
  - `OrderUiTest` + `OrderUiRobot` + `OrderStateRobot`
  - `CategoryDetailUiTest` + `CategoryDetailUiRobot` + `CategoryDetailStateRobot`
- iOS UI Component (`iosAppTests/UIComponent/Order/`):
  - `OrderViewTest`, `OrderViewRobot`, `OrderStateRobot` (ViewInspector)
  - `CategoryDetailViewTest`, `CategoryDetailViewRobot`, `CategoryDetailStateRobot`
- E2E (mobile e2e + iOS UI tests):
  - `testing/e2e-tests/.../OrderJourneyTest` — Android tab → preview list
  - `iosAppE2ETests/Suites/OrderJourneyTest` — iOS tab → preview list
- Fakes (test/):
  - `FakeGetOrderContent` (DEFAULT exposes 6 categoryPreviews)
  - `FakeGetCategoryDetailContent`
  - Per-source fakes (`FakeMenuRemoteDataSource`, `FakeMenuItemLocalDataSource`) live in `impl/data/commonTest` since the data-source interfaces are `internal`.
- E2E and CI must use fakes — never call live Spoonacular (free-tier quota is 150 req/day).
