# Nutrition Feature

## Business Context
The Nutrition screen embeds the per-market McDonald's nutrition calculator in an in-app WebView so users can browse nutrition info without leaving the app. The first real consumer of the WebView primitive in `core:presentation`. Reachable via a contributed entry on the More tab; visible in all builds, all markets.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| NutritionScreen | api/navigation | `data object`, plain `Screen` (not Tab/Protected/Flow). |
| NutritionTestTags | api/navigation (`api/ui` subpackage) | `SCREEN`, `WEBVIEW`, `BACK_BUTTON` constants. |
| NutritionContent | api/domain | `url: String` — sole field. |
| GetNutritionContent | api/domain → impl/domain | `CenterPostSubjectInteractor<Unit, NutritionContent>` (streaming). |
| GetNutritionContentImpl | impl/domain | Forwards `NutritionRepository.getNutrition()` unchanged. |
| NutritionRepository | impl/domain → impl/data | `getNutrition(): Flow<NutritionContent>`. |
| NutritionRepositoryImpl | impl/data | Reads `AppBuildConfig.nutritionUrl`, emits a single `NutritionContent` via `flowOf(...)`. No network. |
| NutritionPresenter | impl/presentation | `@CircuitInject(NutritionScreen)`. Collects content, exposes `BackClicked` event handler that pops the navigator. |
| NutritionUiState | impl/presentation | `data class(url: String?, eventSink)`. `url == null` is the frame-zero state before the synchronous flow emits. |
| NutritionEvent | impl/presentation | `sealed class`; sole entry `BackClicked`. |
| NutritionUi | impl/presentation/androidMain | `Scaffold` + `TopAppBar("Nutrition")` + `WebViewContent(url, allowJs = true)` body. `BackHandler` walks WebView page history before popping the host. |
| FakeGetNutritionContent | test | `@ContributesBinding(AppScope::class)`. `DEFAULT.url = "https://example.test/nutrition"` (sentinel for hermetic tests). |

## Cross-Feature Dependencies
- Navigates to: none (back-only — `BackClicked → navigator.pop()`).
- Imported by: `composeApp` (auto-discovered via `settings.gradle.kts` glob); `features:more:impl:presentation` imports `features:nutrition:api:navigation` to route `MoreEvent.MenuItemClicked(id = "3")` → `goTo(NutritionScreen)`.
- Core deps: `core:centerpost`, `core:circuit`, `core:theme`, `core:build-config:api` (data layer reads `nutritionUrl`), `core:presentation` (WebView primitive auto-wired via `mockdonalds.kmp.presentation` plugin).
- Entry point: the existing hardcoded `MoreMenuItem(id = "3", icon = "🥗", title = "Nutrition")` in `MoreRepositoryImpl`; `MorePresenter` routes id "3" to `NutritionScreen`. **Not** contributed via `MoreTabExtension` — same pattern as Recents (id "1" → `RecentsScreen`).

## Feature-Specific Patterns
- **No network in `impl/data`**. Repository wraps `AppBuildConfig.nutritionUrl` directly via `flowOf`. Build script does NOT depend on `core:network:api`. When v2 introduces a real backend (per-item data, A/B variants), add the network dep then.
- **JS opt-in.** `WebViewContent(allowJs = true)` because the calculator uses interactive controls. Default in the WebView primitive is JS off.
- **Hardcoded English strings.** `"Nutrition"` (top-bar title) and `"Back"` (content description) are inline. Will move to string resources when Phrase + `Localizable.strings` are wired.
- **Page-view tracking is automatic.** No analytics code in this feature. `composeApp/src/commonMain/.../navigation/AnalyticsNavigationListener.kt` fires `trackScreenView("NutritionScreen")` on every `goTo`. If that listener is removed/changed, this feature silently loses tracking — track via the listener's owning module.
- **iOS navigation asymmetry.** Per the WebView primitive's documented contract, iOS edge-swipe always pops host; no "back in WebView" affordance on iOS in v1. The `canGoBack` binding on `WebView` SwiftUI is dead-state on iOS, live on Android via `BackHandler`.
- **Sentinel URL hygiene in tests.** All tests use `https://example.test/nutrition` (RFC2606 reserved domain) to avoid hermetic-test network round-trips.

## Per-Market Configuration
- Default `NUTRITION_URL=https://www.mcdonalds.com/us/en-us/about-our-food/nutrition-calculator.html`.
- `ca-int`, `ca-mte`, `ca-prod` override with the `/ca/en-ca/` variant.
- `us`, `de`, `au`, `core` inherit the default (DE/AU localized variants pending validation).

## Testing
- Unit (presenter): `impl/presentation/src/commonTest/.../NutritionPresenterTest.kt`
- Unit (use case): `impl/domain/src/commonTest/.../GetNutritionContentImplTest.kt`
- Unit (repository): `impl/data/src/commonTest/.../NutritionRepositoryImplTest.kt` — uses generated `FakeAppBuildConfig` from `core:build-config:test`.
- UI Component (Android): `impl/presentation/src/androidDeviceTest/.../{NutritionUiTest, NutritionUiRobot, NutritionStateRobot}.kt`
- UI Component (iOS): `iosApp/iosAppTests/UIComponent/NutritionView*.swift` (ViewInspector Robot pattern).
- Fake: `test/src/commonMain/.../FakeGetNutritionContent.kt` (`DEFAULT.url` is the sentinel).
- Test scope is **non-network only** — tests verify state→props plumbing, never WebView content rendering or actual web fetches.
