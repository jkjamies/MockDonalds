# Spec — Order: Spoonacular Menu Integration

> **Spec type**: `change` (modifying existing `features/order` behavior)
> **Downstream skills**: `/update order @specs/order-spoonacular-integration.md`
> **Status**: ready for implementation (all decisions resolved)

---

## Overview

Replace the hardcoded fake data in `features/order` with a live integration against the Spoonacular `/food/menuItems/search` endpoint. The order tab is restructured: the landing screen becomes a vertical list of categories (with a first-item image preview each), and tapping a category opens a new `CategoryDetailScreen` showing the full item list for that category. Items are cached in SQLDelight with a 24h TTL so the free-tier daily quota is not the per-screen limiter.

This is also the **first networked feature** and the **first SQLDelight consumer** in the codebase. Patterns established here become the reference for future feature integrations.

**Feature**: `order`
**Change type**: enhancement (replaces fake data with live API + introduces persistence + adds new screen)
**Scope**: broad (cross-layer: data, domain, presentation, navigation, build wiring, iOS parity)

---

## Current Behavior

**What the user sees**:
- Single `OrderUi` screen with horizontal category chips at the top (Burgers, Fries, Drinks, Desserts), a vertical list of two hardcoded "featured items" (Midnight Truffle, Saffron Fries), and a floating cart bar showing "2 ITEMS / $36.00".
- Tapping a category chip toggles `selectedCategoryId` but does not change the displayed items — the chip selection is currently a visual no-op.
- "ADD TO ORDER" button on each item card fires a no-op centerPost.

**What the code does**:
- `OrderRepositoryImpl` (`features/order/impl/data`) returns three hardcoded `flowOf(...)` flows for categories, featured items, and cart summary.
- `GetOrderContentImpl` combines the three flows into `OrderContent`.
- `OrderPresenter` collects `OrderContent`, maintains local `selectedCategoryId` via `remember { mutableStateOf }`, exposes `OrderUiState(categories, selectedCategoryId, featuredItems, cartSummary, eventSink)`.
- `OrderUi` (Compose) and `OrderView` (SwiftUI) render the chips + featured-items list + cart bar.
- No HTTP client. No persistence. No `CategoryDetailScreen`.

**Relevant files**:
- `features/order/impl/data/OrderRepositoryImpl.kt` — hardcoded data
- `features/order/impl/domain/{OrderRepository.kt, GetOrderContentImpl.kt}`
- `features/order/api/domain/{OrderModels.kt, GetOrderContent.kt}`
- `features/order/impl/presentation/{OrderPresenter.kt, OrderUiState.kt, OrderUi.kt}` (+ `androidMain` UI)
- `features/order/api/navigation/OrderScreen.kt`, `api/ui/OrderTestTags.kt`
- `features/order/test/FakeGetOrderContent.kt`
- `iosApp/iosApp/Features/Order/OrderView.swift`

---

## Desired Behavior

**What the user should see**:

*Landing (`OrderUi`)* — vertical scroll list of 6 category cards, each card showing:
- Category display name (e.g., "Burgers")
- Image preview from the first item in that category (pulled from Spoonacular)
- Tap target → navigates to `CategoryDetailScreen(categoryId)`
- Floating cart bar (unchanged — still shows the hardcoded "2 ITEMS / $36.00").

*Category Detail (`CategoryDetailScreen`)* — top app bar with category name + back chevron; vertical list of up to 20 menu items, each showing image, title, restaurant chain, optional serving size, and an "ADD TO ORDER" button (no-op centerPost, matching today's pattern). Floating cart bar present here too.

*Failure modes*:
- If SQLDelight cache has data and the network call fails or returns 402 (quota exhausted), show the cached data silently. Error is logged.
- If cache is empty and network fails, the relevant section shows an empty state (no items / no categories) — no toast, no retry button in v1.

**What the code should do**:

1. **Data layer**:
   - `MenuRemoteDataSource` interface + `MenuRemoteDataSourceImpl` calling Spoonacular `/food/menuItems/search?query={q}&number=20`.
   - DTO `SpoonacularMenuItemSearchResponseDto` + `SpoonacularMenuItemDto` (`@Serializable`).
   - Mapper extension `SpoonacularMenuItemDto.toMenuItem(categoryId)` returning domain `MenuItem`.
   - `MenuItemLocalDataSource` reading/writing SQLDelight (`MenuItemQueries` generated from `.sq`).
   - `OrderRepositoryImpl` rewritten to: cache-first read; if cache stale (>24h) or empty, fetch remote, store, emit. On remote failure, emit cache regardless.
   - `OrderRepository` gains `getMenuItemsByCategory(categoryId)` and `getCategoryPreviews()` (returns `List<CategoryPreview>` with first item per category).

2. **Domain layer**:
   - New use case `GetCategoryDetailContent(categoryId)` (streaming, `CenterPostSubjectInteractor<String, CategoryDetailContent>`).
   - Existing `GetOrderContent` updated to emit `OrderContent` with the new `categoryPreviews` shape (replaces flat `featuredItems`).

3. **Presentation layer**:
   - `OrderPresenter` updated: emits `OrderUiState(categoryPreviews, cartSummary, eventSink)`. `CategoryTapped(id)` event navigates to `CategoryDetailScreen(id)`.
   - New `CategoryDetailPresenter` + `CategoryDetailUiState` + `CategoryDetailEvent`.
   - New `CategoryDetailUi` (Compose) + `CategoryDetailView` (SwiftUI).

4. **Navigation**:
   - New `CategoryDetailScreen(categoryId: String)` data class (`@Parcelize`) in `features/order/api/navigation/`.
   - `OrderScreen` remains a `TabScreen`; `CategoryDetailScreen` is a regular `Screen` (push destination).

5. **Network wiring**:
   - First `HttpClient` consumer in the codebase. Bind a Spoonacular-scoped `HttpClient` via Metro provider in `features/order/impl/data`. Configure: `baseUrl = "https://api.spoonacular.com/food"`, `authMode = AuthMode.NONE` (Spoonacular doesn't use Bearer; we set the API key header manually), `headers["x-api-key"] = AppBuildConfig.spoonacularApiKey`.

6. **Persistence wiring**:
   - First SQLDelight consumer. Add `features/order/impl/data/sqldelight/order/com/jkjamies/sampleplatter/features/order/data/MenuItem.sq` with `menuItem` table + queries.
   - Apply SQLDelight plugin in `composeApp` (the aggregator) per CLAUDE.md's persistence pattern, depending on `:features:order:impl:data` so its `.sq` files are picked up.
   - `core:persistence` already provides `DatabaseDriverFactory`; verify it's wired into `AppDatabase` aggregation.

7. **Build config**:
   - Add `spoonacularApiKey: String` to `AppBuildConfig` interface (Group: new `Secrets` group, or reuse `Identity` — see Decisions).
   - **Sourced from `local.properties` (gitignored), not committed defaults.** Empty default so reference-app builds still compile without the key. Comment in BuildKonfig wiring documents the enterprise migration path.

8. **iOS parity**:
   - SwiftUI `CategoryDetailView` exposed via `@CircuitInject`. `OrderView.swift` updated to render the new category-list landing layout.

---

## Affected Layers

- [x] **api/domain** — new `MenuItem`, `CategoryPreview`, `CategoryDetailContent` models; updated `OrderContent`; new `GetCategoryDetailContent` abstract use case
- [x] **api/navigation** — new `CategoryDetailScreen` data class; new `CategoryDetailTestTags` object
- [x] **impl/domain** — new `GetCategoryDetailContentImpl`; updated `GetOrderContentImpl`; updated `OrderRepository` interface (new methods, removed `getFeaturedItems`)
- [x] **impl/data** — new `MenuRemoteDataSource(+Impl)`, `MenuItemLocalDataSource(+Impl)`, DTOs, mapper, SQLDelight `.sq` schema; rewritten `OrderRepositoryImpl`; Metro provider for Spoonacular `HttpClient`
- [x] **impl/presentation (common)** — updated `OrderPresenter`, `OrderUiState`, `OrderEvent`; new `CategoryDetailPresenter`, `CategoryDetailUiState`, `CategoryDetailEvent`
- [x] **impl/presentation (androidMain)** — rewritten `OrderUi` (vertical category list); new `CategoryDetailUi`
- [x] **impl/presentation (iosMain)** — KMP-NativeCoroutines exposes new presenter; `OrderView.swift` rewritten + new `CategoryDetailView.swift`
- [x] **test/** — new `FakeGetCategoryDetailContent`; updated `FakeGetOrderContent`; new `FakeMenuRemoteDataSource`, `FakeMenuItemLocalDataSource`
- [x] **core module(s)** — none modified (uses existing `core:network`, `core:persistence`, `core:build-config`, `core:circuit`, `core:centerpost`)
- [x] **build-config** — new `spoonacularApiKey` field via `/add-config-field` skill (with caveat: empty default, sourced from `local.properties`)
- [x] **iosApp (SwiftUI)** — `OrderView.swift` rewritten; new `CategoryDetailView.swift`; bridge wiring
- [x] **composeApp** — apply SQLDelight plugin (first time), depend on `:features:order:impl:data` for schema aggregation; Metro `HttpClient` provider

---

## Domain Model Changes

### Removed Models

```
FeaturedItem
  - id, title, price, description, imageUrl, tag, isPrimary
  // Replaced by MenuItem; no concept of "isPrimary" or "tag" — Spoonacular doesn't return those.
  // "Featured" becomes one of the categories in the canonical list.
```

### Changed Models

```
OrderContent
  ~ featuredItems: List<FeaturedItem>  →  categoryPreviews: List<CategoryPreview>
  ~ // categories field removed; CategoryPreview already includes (id, name)
  // cartSummary: CartSummary unchanged
```

### New Models

```
MenuItem
  ├── id: String                     // Spoonacular item id, stringified
  ├── title: String                  // e.g. "Big Mac"
  ├── restaurantChain: String        // e.g. "Sample Platter"
  ├── imageUrl: String               // Spoonacular CDN URL
  ├── servingSize: String?           // e.g. "1 sandwich (214g)" — sometimes null
  └── categoryId: String             // FK to CategoryPreview.id; assigned at mapper time

CategoryPreview
  ├── id: String                     // "featured" | "burgers" | "chicken" | "sides" | "drinks" | "desserts"
  ├── name: String                   // display name, e.g. "Burgers"
  ├── firstItemImageUrl: String?     // image of the first cached item, null if cache empty
  └── itemCount: Int                 // how many items are cached for this category (0 = empty / not yet fetched)

CategoryDetailContent
  ├── categoryId: String
  ├── categoryName: String
  └── items: List<MenuItem>
```

Domain models stay annotation-free — `@Serializable` lives only on DTOs in `impl/data`.

---

## API / Network Changes

### New Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/food/menuItems/search?query={q}&number=20` | x-api-key header | Returns up to 20 menu items matching the query. Called once per category on cache miss / staleness. |

### DTO Definitions

```
SpoonacularMenuItemSearchResponseDto
  ├── menuItems: List<SpoonacularMenuItemDto>
  ├── number: Int                    // returned page size
  ├── offset: Int                    // (always 0 in v1; we don't paginate)
  └── totalMenuItems: Int            // ignored in v1

SpoonacularMenuItemDto
  ├── id: Int                        // Spoonacular item id (numeric in API; we stringify in mapper)
  ├── title: String
  ├── restaurantChain: String
  ├── image: String                  // → imageUrl in domain
  ├── servingSize: String?           // optional in API
  └── // other Spoonacular fields (badges, link, etc.) ignored — not in domain
```

All DTOs are `@Serializable` data classes in `commonMain`, suffixed `Dto`.

### Mapper

```kotlin
internal fun SpoonacularMenuItemDto.toMenuItem(categoryId: String): MenuItem = MenuItem(
    id = id.toString(),
    title = title,
    restaurantChain = restaurantChain,
    imageUrl = image,
    servingSize = servingSize,
    categoryId = categoryId,
)
```

### Category → Query Mapping (in `OrderRepositoryImpl`)

| Category id | Display name | Spoonacular query |
|-------------|--------------|-------------------|
| `featured`  | Featured     | `popular`         |
| `burgers`   | Burgers      | `burger`          |
| `chicken`   | Chicken      | `chicken sandwich`|
| `sides`     | Sides        | `fries`           |
| `drinks`    | Drinks       | `soda`            |
| `desserts`  | Desserts     | `ice cream`       |

Static `private val` list of `(id, displayName, query)` tuples in `OrderRepositoryImpl`.

### HttpClient Configuration

```kotlin
// In features/order/impl/data, Metro @Provides for the Spoonacular client.
@Provides
@SingleIn(AppScope::class)
@SpoonacularHttpClient   // qualifier to disambiguate from other future clients
fun provideSpoonacularClient(
    factory: HttpClientFactory,
    buildConfig: AppBuildConfig,
): HttpClient = factory.create {
    baseUrl = "https://api.spoonacular.com/food"
    authMode = AuthMode.NONE
    header("x-api-key", buildConfig.spoonacularApiKey)
}
```

The `x-api-key` header is set on this client's `defaultRequest` — scope is per-HttpClient-instance, so it does not leak to other features that build their own clients from the same factory.

### Akamai Exception (documented in spec + AGENTS.md)

> Spoonacular is a third-party reference integration. It does **not** route through Akamai. The "Akamai required in all envs" rule applies to first-party SamplePlatter backend services only — third-party reference APIs (Spoonacular, etc.) call the public endpoint directly. This exception must be documented in `features/order/impl/data/AGENTS.md`.

---

## SQLDelight Schema

`features/order/impl/data/src/commonMain/sqldelight/com/jkjamies/sampleplatter/features/order/data/MenuItem.sq`

```sql
CREATE TABLE menuItem (
    id TEXT NOT NULL,
    categoryId TEXT NOT NULL,
    title TEXT NOT NULL,
    restaurantChain TEXT NOT NULL,
    imageUrl TEXT NOT NULL,
    servingSize TEXT,
    cachedAt INTEGER NOT NULL,           -- epoch millis when this row was cached
    PRIMARY KEY (id, categoryId)         -- same item could appear in multiple categories
);

CREATE INDEX menuItem_categoryId ON menuItem(categoryId);

selectByCategory:
SELECT * FROM menuItem WHERE categoryId = :categoryId ORDER BY rowid ASC;

selectFirstByCategory:
SELECT * FROM menuItem WHERE categoryId = :categoryId ORDER BY rowid ASC LIMIT 1;

countByCategory:
SELECT COUNT(*) FROM menuItem WHERE categoryId = :categoryId;

oldestCachedAtByCategory:
SELECT MIN(cachedAt) FROM menuItem WHERE categoryId = :categoryId;

insertItem:
INSERT OR REPLACE INTO menuItem(id, categoryId, title, restaurantChain, imageUrl, servingSize, cachedAt)
VALUES (?, ?, ?, ?, ?, ?, ?);

deleteByCategory:
DELETE FROM menuItem WHERE categoryId = :categoryId;
```

Cache-staleness rule: `oldestCachedAtByCategory < (now - 24h)` → refetch and replace via `deleteByCategory` + `insertItem`.

`composeApp` becomes the SQLDelight aggregator: applies the `app.cash.sqldelight` plugin, declares `databases { create("AppDatabase") { … dependency(project(":features:order:impl:data")) } }`. Konsist `PersistenceConventionsTest` already restricts SQLDelight imports to `core:persistence`, `composeApp`, and feature `impl/data` — this change stays inside that whitelist.

---

## Use Case Changes

| Use Case | Change | Details |
|----------|--------|---------|
| `GetOrderContent` | Modified | Now combines `getCategoryPreviews()` flow + `getCartSummary()` flow only. Drops `getFeaturedItems()`. |
| `GetCategoryDetailContent` | New | `CenterPostSubjectInteractor<String, CategoryDetailContent>` — streaming, takes categoryId param, emits items list + display name. |

### Updated Data Flow — `GetOrderContent`

```
GetOrderContent
  ├── repository.getCategoryPreviews()         ← reads cache, lazily warms via remote on demand
  └── combine { previews, cart ->
        OrderContent(categoryPreviews = previews, cartSummary = cart)
      }
```

### New Data Flow — `GetCategoryDetailContent`

```
GetCategoryDetailContent(categoryId)
  ├── repository.getMenuItemsByCategory(categoryId)
  │     ├── if cache fresh → emit cached items
  │     ├── if cache stale or empty → fetch /food/menuItems/search?query=Q
  │     │     ├── on success → store in SQLDelight, emit
  │     │     └── on failure → emit cached items (possibly empty)
  │     └── return Flow<List<MenuItem>>
  └── map { items -> CategoryDetailContent(categoryId, name, items) }
```

---

## UI / Presenter Changes

### OrderUiState (modified)

```
OrderUiState
  - categories: List<MenuCategory>            // REMOVED
  - selectedCategoryId: String?               // REMOVED (no inline filter anymore)
  - featuredItems: List<FeaturedItem>         // REMOVED
  + categoryPreviews: List<CategoryPreview>   // NEW
    cartSummary: CartSummary?                 // unchanged
    eventSink: (OrderEvent) -> Unit           // unchanged shape, new event payload
```

### OrderEvent (modified)

```
OrderEvent
  - CategorySelected(id: String)              // REMOVED
  - AddToOrder(itemId: String)                // REMOVED (moves to CategoryDetailEvent)
  + CategoryTapped(id: String)                // NEW — navigates to CategoryDetailScreen
    CartClicked                               // unchanged
```

### CategoryDetailUiState (new)

```
CategoryDetailUiState(
    categoryId: String,
    categoryName: String,
    items: List<MenuItem>,
    cartSummary: CartSummary?,
    eventSink: (CategoryDetailEvent) -> Unit,
) : CircuitUiState
```

### CategoryDetailEvent (new)

```
sealed class CategoryDetailEvent {
    data object BackPressed : CategoryDetailEvent()
    data class AddToOrder(val itemId: String) : CategoryDetailEvent()
    data object CartClicked : CategoryDetailEvent()
}
```

### TestTags

`OrderTestTags` (modified):
- KEEP: `CART_BAR`
- REMOVE: `CATEGORY_CHIP`, `FEATURED_ITEMS_SECTION`, `FEATURED_ITEM_CARD`, `ADD_TO_ORDER_BUTTON`
- ADD: `CATEGORY_PREVIEW_CARD` (suffixed with categoryId at use site, matching current pattern)

`CategoryDetailTestTags` (new):
- `TOP_BAR`
- `BACK_BUTTON`
- `MENU_ITEM_CARD` (suffixed with itemId)
- `ADD_TO_ORDER_BUTTON` (suffixed with itemId)
- `CART_BAR`

### UI Layout

*OrderUi* — vertical scroll `Column` of `CategoryPreviewCard`s. Each card: large image (or surface placeholder if `firstItemImageUrl` is null), category name overlay, tap → `CategoryTapped(id)`. Floating cart bar unchanged. No category chips at top anymore.

*CategoryDetailUi* — `TopAppBar(title=categoryName, navigationIcon=back chevron)`. `LazyColumn` of `MenuItemCard`s: image, title, restaurant chain, optional serving size, "ADD TO ORDER" button. Floating cart bar at bottom (same as Order tab).

---

## Navigation Changes

| Change | From | To |
|--------|------|----|
| New destination | — | `CategoryDetailScreen(categoryId: String)` — `@Parcelize` data class implementing `Screen` |
| New nav event | — | `OrderEvent.CategoryTapped(id)` → `navigator.goTo(CategoryDetailScreen(id))` |
| Auth gating | none | none — menu browsing is public |
| Back behavior | n/a | `CategoryDetailEvent.BackPressed` → `navigator.pop()` |

`CategoryDetailScreen` is a regular `Screen`, not `TabScreen` — it's a push destination from the order tab.

iOS bridge wiring: the new screen is registered through the existing `BridgeNavigator` mechanism. (Codebase pattern is the established `@CircuitInject` + Swift bridge — implementer follows the same shape `OrderView` uses today.)

---

## Build Config Changes

| Field | Change | Details |
|-------|--------|---------|
| `spoonacularApiKey: String` | New | Empty default in `Defaults.properties`. **Not** copied into per-market files (single key for the reference app). Sourced from `local.properties` at build time via Gradle override. |

### local.properties contract

```
# local.properties (gitignored — already in .gitignore)
spoonacularApiKey=YOUR_KEY_HERE
```

Gradle in `core/build-config/impl/build.gradle.kts` (or wherever BuildKonfig is configured) reads `local.properties.getProperty("spoonacularApiKey", "")` and feeds it into `BuildConfig.SPOONACULAR_API_KEY`. `AppBuildConfigImpl.spoonacularApiKey` reads `BuildConfig.SPOONACULAR_API_KEY`.

A code comment near the BuildKonfig wiring:

```kotlin
// spoonacularApiKey is sourced from local.properties (gitignored) so the public
// reference repo stays clean. In an enterprise/internal build, move this to
// core/build-config/markets/{market}/{env}.properties so it's per-env and CI-managed.
```

`spoonacularApiKey` is **excluded** from the debug-config viewer (no `@DebugConfigField` annotation, or annotated with a `Group.Secrets` that's filtered out of debug output). Surfacing API keys in a debug menu is a leak risk.

---

## Test Impact

### New Test Scenarios

**Unit (Kotest BehaviorSpec, host)**:
- [ ] `OrderRepositoryImplTest` (rewritten):
  - cache hit returns cached items without calling remote
  - cache stale (>24h) triggers remote fetch + replace
  - cache miss triggers remote fetch + insert
  - remote failure with non-empty cache → emits cache, no error propagation
  - remote failure with empty cache → emits empty list, error logged via `Logger`
  - `getCategoryPreviews()` returns 6 entries even when no items are cached (empty preview)
- [ ] `SpoonacularMenuItemDtoMapperTest` — happy path + servingSize null
- [ ] `GetOrderContentImplTest` (updated) — combines categoryPreviews + cartSummary, drops featuredItems references
- [ ] `GetCategoryDetailContentImplTest` (new) — emits domain content for a given categoryId
- [ ] `OrderPresenterTest` (updated) — exposes `categoryPreviews`; `CategoryTapped(id)` navigates to `CategoryDetailScreen(id)`
- [ ] `CategoryDetailPresenterTest` (new) — emits items + name; `BackPressed` pops; `AddToOrder` no-ops centerPost; `CartClicked` no-ops

**UI Component (Compose Robot pattern, Android device)**:
- [ ] `OrderUiTest` (rewritten) — renders 6 category cards; tapping fires `CategoryTapped`; cart bar present
- [ ] `OrderUiRobot` + `OrderStateRobot` (rewritten) — preview-list state configurations
- [ ] `CategoryDetailUiTest` (new) — renders top bar, item list, back button; tapping back fires `BackPressed`
- [ ] `CategoryDetailUiRobot` + `CategoryDetailStateRobot` (new)
- [ ] iOS counterparts (ViewInspector): `OrderViewTest`, `CategoryDetailViewTest`

**Navint (real Circuit + fake data layer)**:
- [ ] `OrderNavIntTest` (Android + iOS) — tab → category detail → back flow asserts `OrderScreen` ←→ `CategoryDetailScreen("burgers")` transitions

**E2E (AppRobot, full journey)**:
- [ ] `OrderE2ETest` (Android + iOS) — open order tab, tap a category, verify item list renders, tap back, return to landing. Uses fake data layer (no real Spoonacular call) — quota safety. (User accepted E2E in the test scope question.)

**Architecture (Konsist + Harmonize)**:
- Existing rules cover this — no new architecture tests needed:
  - `@Serializable` only on DTOs (existing rule applies)
  - DTOs suffixed `Dto` (existing rule applies)
  - SQLDelight imports restricted to `core:persistence` / `composeApp` / feature `impl/data` (existing `PersistenceConventionsTest` rule applies)
  - `@ContributesBinding` on impls (existing rule applies)

### Fakes to Update / Add

- [ ] `FakeGetOrderContent` (updated) — DEFAULT replaces `featuredItems` with `categoryPreviews` (6 entries)
- [ ] `FakeGetCategoryDetailContent` (new) — emits configurable `CategoryDetailContent`
- [ ] `FakeMenuRemoteDataSource` (new) — controllable `searchMenuItems(query)` for repository tests
- [ ] `FakeMenuItemLocalDataSource` (new) — in-memory store for repository tests; bypasses real SQLDelight
- [ ] iOS test helpers updated to match new state shapes

---

## Constraints & Considerations

- **First networked feature in the codebase**: this implementation is the reference for how all future features wire up `HttpClientFactory`. Be deliberate — the patterns set here will be copied. Document the Spoonacular client provider clearly.
- **First SQLDelight consumer**: composeApp aggregator wiring + feature-owned `.sq` schemas pattern is established here. Verify `PersistenceConventionsTest` still passes.
- **Akamai exception**: Spoonacular is third-party — does not route through Akamai. Document in `features/order/impl/data/AGENTS.md`. The Akamai-in-all-envs rule still binds for first-party backends.
- **Quota economics (150/day free tier)**: 6 categories × 1 call to warm = 6 calls per cache build. With 24h TTL, a developer running daily uses ~6/day. CI/E2E must use `FakeMenuRemoteDataSource` — never hit live Spoonacular in tests.
- **API key safety**: never commit a real key. `local.properties` is gitignored. Empty default. `spoonacularApiKey` excluded from `@DebugConfigField` to keep it out of any debug menu.
- **iOS parity**: The codebase has been kept symmetric — Compose UI ↔ SwiftUI bridge. Both `OrderView.swift` and the new `CategoryDetailView.swift` must ship in the same change.
- **Cart summary stays hardcoded**: `CartSummary(itemCount=2, total="$36.00")` remains a static `flowOf`. `AddToOrder` events remain no-op centerPost. Real cart wiring is a separate spec.
- **Performance**: SQLDelight reads on cold start should fit budget — these are small tables (max ~120 rows = 6 categories × 20 items). No pagination, no streaming.
- **Logging**: Remote fetch failures are logged via `core:logger:api` (already auto-wired into `kmp.data` modules). Don't suppress.
- **Image loading**: Existing Coil setup (already wired into `kmp.presentation`) handles `imageUrl`. No new dependency.
- **Konsist Forbidden patterns**: no wildcard imports, no `println`, no `!!`, no raw coroutine primitives, no `lateinit var` in shared code — all already enforced.

---

## Out of Scope

- Real cart functionality (add-to-order persistence, cart contents screen, checkout). `CartSummary` remains hardcoded; `AddToOrder` events remain no-op centerPost.
- Item detail screen (calories, full nutrition, ingredients). Requires a second Spoonacular call to `/food/menuItems/{id}` — explicitly deferred for quota economics.
- Pagination / infinite scroll. Capped at 20 items per category.
- Pull-to-refresh on category detail.
- Category-specific UI tweaks (different colors, hero images per category, etc.). Categories share the same card layout.
- Per-env Spoonacular keys (single `local.properties` entry for the reference app).
- Routing Spoonacular through Akamai. Documented exception only.
- Distinct "402 quota exhausted" UI. Quota errors are silent fallback to cache.
- Localization of category names. Hardcoded English in v1; will move to `core:strings` when the broader localization sweep happens.
- Remote-driven category list (e.g., from Harness). Categories hardcoded in `OrderRepositoryImpl`.
- Replacing the existing `CartSummary` floating bar styling.
- Removing `MenuCategory` and `FeaturedItem` from any other features that import them. (Quick check: `features/order` is the only consumer per AGENTS.md "Imported by: composeApp" — confirm during implementation.)

---

## Decisions

Resolved during the grill phase. Each entry: question → answer → rationale.

1. **Navigation model** — Inline filter vs. separate screen?
   → **Separate `CategoryDetailScreen`**. Cleaner if categories grow rich; deep-linkable; matches the user's intent literally ("the actual items are on each of the category pages").

2. **Fate of FeaturedItems section** — Keep, replace, or remove?
   → **Remove the section; "Featured" becomes one of the categories.** Landing becomes a vertical list of category cards with first-item preview each. SQLDelight caching eliminates the quota concern of "one call per category on landing."

3. **Category list source** — Hardcoded in repo / build-config / remote?
   → **Hardcoded in `OrderRepositoryImpl`.** Static list of `(id, displayName, query)` tuples. Build-config / remote is overkill for a stable taxonomy in a reference app.

4. **Shipping categories** — Which 6?
   → **Featured, Burgers, Chicken, Sides, Drinks, Desserts.** "Featured" maps to the `popular` Spoonacular query.

5. **MenuItem fields** — Search-only vs. with nutrition?
   → **Search-only**: id, title, restaurantChain, imageUrl, servingSize?, categoryId. Skips the per-item detail call to preserve quota.

6. **Cache policy** — TTL vs. never-expire vs. SWR vs. in-memory?
   → **Cache-first with 24h TTL, no manual refresh.** Predictable quota. No pull-to-refresh in v1.

7. **API key storage** — build-config-committed vs. local.properties vs. env var?
   → **`local.properties` (gitignored) read at build time, with a code comment documenting the enterprise migration path to per-env build-config.** Public reference repo stays clean; pattern is upgradeable.

8. **Auth scope** — Header on default request vs. per-call vs. query param?
   → **`x-api-key` on the Spoonacular HttpClient's `defaultRequest`.** Per-HttpClient-instance scope means no leak to other features' clients. Confirmed during grilling that the user understood this scoping.

9. **Quota / error handling** — Cached fallback vs. distinct UI vs. crash?
   → **Cached fallback + silent log.** No special quota UI. Empty state if cache is empty. Reference app doesn't need an educational quota-exhausted screen.

10. **Pagination** — First N vs. paginate?
    → **First 20 items per category, no pagination.** `number=20` query param.

11. **Test coverage** — Which levels?
    → **All four**: unit, UI component (Robot), navint, E2E. User explicitly opted in to E2E despite the recommendation that navint covered the flow.

12. **Item-tap behavior on category detail** — Add to cart / nav to detail / read-only?
    → **Add to cart no-op centerPost**, matching the existing pattern.

13. **Cart summary** — Live, hardcoded, or remove?
    → **Keep hardcoded.** Cart wiring is a separate spec.

14. **iOS parity** — Full / Android-only / shared Compose?
    → **Full parity.** New `CategoryDetailView.swift` ships in the same change as the Compose `CategoryDetailUi`.

15. **Akamai exception** — Document where?
    → **Spec + `features/order/impl/data/AGENTS.md`.** Future readers see the rule wasn't forgotten.

### Deferred

None. All open questions resolved.

---

## Implementation Notes for Downstream Skill

When `/update order @specs/order-spoonacular-integration.md` runs:

1. **Order matters**: do data layer first (DTO + mapper + remote DS + SQLDelight schema + repository rewrite), then domain (use case updates), then presentation (presenter + UI), then iOS bridge, then tests, then `/add-config-field spoonacularApiKey` as a one-off.
2. **composeApp wiring** is non-trivial because it's the first SQLDelight consumer — apply the plugin block, declare the `AppDatabase` aggregation, register `:features:order:impl:data`. Verify `:composeApp:assembleDebug` succeeds before moving on to wiring the Metro `HttpClient` provider.
3. **Run `verify diff` between major milestones** — don't try to land the entire change without intermediate verification. The spec's blast radius is broad (cross-layer + first-of-kind infrastructure).
4. **Update `features/order/AGENTS.md`** — replace the entire "Key Types" table to reflect the new domain model. Add the Akamai exception note.
5. **Pull translations** — not needed in v1 (English only); category names are inline string constants.
6. **Final step**: `verify full` before declaring done.

---

## Original Requirements

> **Source**: inline conversation
> **Converted on**: 2026-05-09
> **Spec type**: `change` (explicit — modifies existing `features/order`)

<details>
<summary>Original conversation excerpt (click to expand)</summary>

User: "Is there a public API we can use to do a sample for the menu (order) tab items?"

Assistant recommended TheMealDB and Spoonacular.

User: "I can manage an api key if it is significantly closer to our intended usecase?"

Assistant proposed Spoonacular's `/food/menuItems/search` (returns real fast-food chain items: brand, image, serving size, calories, sometimes price).

User: "this will work for build config base and data layer path building? yeah go ahead and implement and then i can do the api keys if you give a few instructions after — should we start with a spec and grill?"

User: "we'll want to keep categories for the order tab, the actual items are on each of the category pages — where maybe the page is shared for everything but the list of items is based on category if that makes sense"

Followed by a 7-round grill (15 resolved decisions) producing this spec.

</details>
