# Spec — `features/nutrition`

> **Spec type**: `new` (no existing `features/nutrition` module)
> **Downstream skills**: `/add-config-field` (one-off, for `nutritionUrl`) → `/add-feature` (scaffolds the module set + files from this spec)
> **Status**: ready for implementation (all 10 grill questions resolved)

---

## Overview

`features/nutrition` is the first consumer of the WebView primitive shipped in `core:presentation`. It exposes a single Circuit destination — `NutritionScreen` — that renders a third-party nutrition reference inside an embedded WebView. Reachable via a contributed entry on the More tab; no API, no domain logic, no feature flag — just a typed destination with a build-config-supplied URL.

**Name**: `nutrition`
**Primary screen**: `NutritionScreen`
**Skill target**: `/add-feature`

---

## Business Context

The nutrition reference is a publicly available web page from Spoonacular, the same provider backing the order feature's menu data. Embedding it inside the app via WebView lets users access nutrition info without leaving the app — better UX than punting to system browser, and matches how every other major QSR app handles this surface.

This feature is also the first real consumer of the WebView primitive that landed in `core:presentation`, validating the cross-feature reusability of that primitive.

**User story**: As a SamplePlatter app user, I want to view nutrition information for the menu so that I can make informed food choices without leaving the app.

**Acceptance criteria**:
- [ ] The existing "🥗 Nutrition" entry (id `"3"`) in the More tab navigates to `NutritionScreen` when tapped — wired via `MorePresenter`'s `MenuItemClicked` event handler, same pattern as id `"1"` → `RecentsScreen`.
- [ ] `NutritionScreen` renders an embedded WebView pointing at the build-config nutrition reference URL.
- [ ] Top app bar shows the title "Nutrition" with a back chevron that returns to the More tab.
- [ ] On Android, the WebView's internal back history is wired via `BackHandler` — system back walks page history before popping the screen.
- [ ] On iOS, the standard `NavigationStack` chevron / edge-swipe pops to More (per the WebView primitive's documented iOS asymmetry).
- [ ] JavaScript is enabled inside the WebView (the calculator uses interactive controls).
- [ ] Off-host links inside the page (e.g., social media, terms of service) open in Custom Tabs (Android) / system browser (iOS), not in-frame.
- [ ] The build-config field `nutritionUrl` defaults to `https://spoonacular.com`. All markets inherit the default; the per-market override mechanism remains available if a market-specific nutrition destination is ever configured.
- [ ] Page-view tracking fires automatically via `AnalyticsNavigationListener` on `goTo(NutritionScreen)` — no per-feature analytics code.

---

## Domain Models

### Primary Model

```
NutritionContent
  └── url: String                  // resolved per-market URL from AppBuildConfig.nutritionUrl
```

That's the entire model. No supporting types, no enums. Top-bar title `"Nutrition"` is a UI-layer constant, not domain data — it lives in the Compose UI / SwiftUI view, will move to `core:strings` when localization lands.

---

## API / Network

n/a — there are no endpoints. The URL is compile-time market config, not server data.

**Base URL config field**: not applicable. Instead, see [Build Config](#build-config) — `nutritionUrl` is the full URL, not a base.

---

## Use Cases

| Name | Type | Params | Result | Description |
|------|------|--------|--------|-------------|
| `GetNutritionContent` | `CenterPostSubjectInteractor` (streaming) | `Unit` | `NutritionContent` | Observe the per-market nutrition URL |

### Data Flow

```
GetNutritionContent
  └── repository.getNutrition()  →  Flow<NutritionContent>
                                    (synchronous; emits exactly once with AppBuildConfig.nutritionUrl)
```

`GetNutritionContentImpl` delegates to `NutritionRepository` and forwards the `Flow<NutritionContent>` unchanged. No combine, no transform.

---

## Repository

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getNutrition()` | `Flow<NutritionContent>` | Stream the resolved per-market URL |

### Implementation

`NutritionRepositoryImpl` is constructor-injected with `AppBuildConfig` and emits a single `NutritionContent(appBuildConfig.nutritionUrl)` via `flowOf(...)`. No remote fetch, no caching, no DTO/mapper. The repo exists primarily as a seam: future v2 (per-item nutrition data, A/B URL variants, market-specific feature flags) slot in behind this interface without changing presenter contracts.

### Data Sources

- [ ] Remote — n/a in v1
- [ ] Local — n/a in v1
- [x] **Combined** — n/a; sole source is `core:build-config:api`

---

## Screen & UI

**Screen type**: `Screen` (data object, no params).
**Tab tag**: n/a (not a `TabScreen`).
**Auth gated**: no.

### Screen definition

```kotlin
// features/nutrition/api/navigation/.../NutritionScreen.kt
@Parcelize
data object NutritionScreen : Screen
```

### UI States

```kotlin
data class NutritionUiState(
    val url: String?,                        // null while flow hasn't emitted (sub-frame); non-null thereafter
    val eventSink: (NutritionEvent) -> Unit,
) : CircuitUiState
```

Single state class, no Loading/Empty/Error variants. Reasoning: the URL flow emits synchronously from `AppBuildConfig`, so `url == null` is a frame-zero artifact, not a real loading state. The WebView primitive owns its own loading UX once `url` is non-null.

### Events

```kotlin
sealed class NutritionEvent : CircuitUiEvent {
    data object BackClicked : NutritionEvent()
}
```

`BackClicked` covers both the Android top-bar back button and any iOS toolbar back. WebView internal-history back is handled by `BackHandler` in the UI itself (Android) or by the system NavigationStack chevron (iOS) — not via this event.

### UI Description (Android — Compose)

`NutritionUi.kt` lives in `features/nutrition/impl/presentation/src/androidMain/.../`:

```
@CircuitInject(NutritionScreen::class, AppScope::class)
@Composable
fun NutritionUi(state: NutritionUiState, modifier: Modifier = Modifier) {
    val webViewState = rememberWebViewState()
    BackHandler(enabled = webViewState.canGoBack) { webViewState.goBack() }

    Scaffold(
        modifier = modifier.testTag(NutritionTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text("Nutrition") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(NutritionEvent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val url = state.url) {
            null -> Box(Modifier.fillMaxSize().padding(innerPadding))
            else -> WebViewContent(
                url = url,
                allowJs = true,
                state = webViewState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag(NutritionTestTags.WEBVIEW),
            )
        }
    }
}
```

- **Top app bar**: title `"Nutrition"` (hardcoded English v1; localization TODO when Phrase wires up), back chevron dispatches `BackClicked`.
- **Body when `url == null`**: empty `Box` for the single sub-frame before flow emits. No spinner — the flow is synchronous from build-config.
- **Body when `url != null`**: `WebViewContent` with `allowJs = true`, host-wired `WebViewState` for back-history coordination, fills available bounds.
- **`BackHandler`**: enabled when WebView has internal history; system back walks page history before propagating to host.

### UI Description (iOS — SwiftUI)

`NutritionView.swift` lives in `iosApp/iosApp/Features/Nutrition/`:

```
private let tags = NutritionTestTags.shared

@CircuitInject(NutritionScreen.self, NutritionUiState.self)
struct NutritionView: View {
    let state: NutritionUiState
    @Environment(\.samplePlatterColors) private var colors
    @State private var canGoBack: Bool = false

    var body: some View {
        Group {
            if let url = state.url.flatMap(URL.init(string:)) {
                WebView(url: url, allowJs: true, canGoBack: $canGoBack)
                    .accessibilityIdentifier(tags.WEBVIEW)
            } else {
                VStack {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: colors.primary))
                    Spacer()
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.background)
        .accessibilityIdentifier(tags.SCREEN)
        .navigationTitle("Nutrition")
        .navigationBarTitleDisplayMode(.inline)
    }
}
```

- **Standard `NavigationStack` chevron** handles return-to-More — no custom toolbar back button, matching `RecentsView`'s pattern.
- **`canGoBack` binding**: declared (required by `WebView` API) but unread. iOS edge-swipe always pops host per the WebView spec's documented platform contract.
- **Initial frame**: centered `ProgressView` while `state.url == nil`, switches to `WebView` once non-null. Mirrors how `RecentsView.Loading` renders.
- **Theme**: `samplePlatterColors` env binding for background, matching every other feature view.

### Test Tags

```kotlin
// features/nutrition/api/navigation/.../ui/NutritionTestTags.kt
object NutritionTestTags {
    const val SCREEN = "nutrition_screen"
    const val WEBVIEW = "nutrition_webview"
}
```

iOS bridge: `NutritionTestTags.shared` exposed via the standard KMP NSObject bridge (see `RecentsTestTags.shared` precedent).

---

## Navigation

**Entry points**:
- The existing hardcoded `MoreMenuItem(id = "3", icon = "🥗", title = "Nutrition")` in `MoreRepositoryImpl` — already in the More tab list. `MorePresenter` routes id `"3"` → `goTo(NutritionScreen)` (same pattern as id `"1"` → `RecentsScreen`).
- No deep link in v1.
- No outbound navigation from this screen besides `navigator.pop()` on `BackClicked`.

**MorePresenter wiring**:

```kotlin
// features/more/impl/presentation/.../MorePresenter.kt — existing presenter, add nutrition branch
is MoreEvent.MenuItemClicked -> {
    val extension = extensionsById[event.id]
    if (extension != null) {
        extension.onClick(navigator)
    } else when (event.id) {
        "1" -> navigator.goTo(RecentsScreen)
        "3" -> navigator.goTo(NutritionScreen)        // ← new
    }
}
```

`features:more:impl:presentation/build.gradle.kts` gains an `implementation(project(":features:nutrition:api:navigation"))` dep so the import resolves.

This is **not** a `MoreTabExtension` contribution. The `MoreTabExtension` pattern is for features that want to inject themselves into the More menu without `MorePresenter` knowing about them (e.g., debug-menu, which is debug-only). Nutrition is a permanent core entry, so it lives in the hardcoded `MoreRepositoryImpl` menu list and `MorePresenter` routes its id directly — same as Recents.

---

## Analytics

**Page-view tracking is automatic.** `composeApp/src/commonMain/.../navigation/AnalyticsNavigationListener.kt` listens to every Circuit `goTo` / `resetRoot` and calls `analyticsDispatcher.trackScreenView(screen::class.simpleName)`. So:

- The moment `NutritionScreen` is in the navigation graph (which it is by virtue of the `@CircuitInject` presenter), navigating to it fires `trackScreenView("NutritionScreen")` automatically.
- Same on iOS — the iOS bridge feeds Circuit nav events through the same listener mechanism.
- **No analytics code in this feature.** No `TrackAnalyticsEvent` injection in the presenter, no `AnalyticsEvent` sealed-class entry, no test stubs for analytics dispatchers.

Per-feature analytics events (button taps, completed flows) would still be feature-owned, but nutrition has no such custom events in v1.

---

## Feature Flags

n/a — nutrition is always on. Removing it requires a code change (delete `NutritionTabExtension`), not a config toggle.

---

## Build Config

| Field | Default | Per-Market | Description |
|-------|---------|------------|-------------|
| `nutritionUrl` | `https://spoonacular.com` | overridable, none set | Nutrition reference URL |

### Per-market values

| Market | `NUTRITION_URL` value |
|--------|------------------------|
| `us` | inherits default |
| `ca` | inherits default |
| `de` | inherits default |
| `au` | inherits default |
| `core` | inherits default (synthetic sandbox market) |

### Files to change

- `core/build-config/impl/Defaults.properties` — add `NUTRITION_URL=https://spoonacular.com`
- Per-market property files: no override needed — every market inherits the default.
- `core/build-config/api/src/commonMain/.../AppBuildConfig.kt` — add `@DebugConfigField(Group.Urls) val nutritionUrl: String`
- `core/build-config/impl/.../AppBuildConfigImpl.kt` — wire field from BuildKonfig
- `core/build-config/impl/build.gradle.kts` BuildKonfig block — add `NUTRITION_URL` field mapping
- `core/build-config/impl/src/commonTest/.../AppBuildConfigSmokeTest.kt` (Phase 1 smoke) — add the field to its assertion list

The `/add-config-field` skill automates all of the above. Run it before `/add-feature`:

```
/add-config-field nutritionUrl url --default "https://spoonacular.com"
```


---

## Cross-Feature Dependencies

**Imports from**:
- `core:circuit` — `Screen`, `Navigator`, `@Parcelize`
- `core:centerpost` — `CenterPostSubjectInteractor` (auto-wired via `sampleplatter.kmp.domain` plugin)
- `core:presentation` — `WebViewContent`, `WebViewState`, `rememberWebViewState` (auto-wired via `sampleplatter.kmp.presentation` plugin; Android-only deps)
- `core:build-config:api` — `AppBuildConfig` (read in `NutritionRepositoryImpl`)
- `core:theme` — Compose `MaterialTheme.colorScheme`, `PlatterDimens` (Android); `samplePlatterColors` env (iOS)

**Imported by**:
- `composeApp` — graph wiring; the convention plugin auto-discovers via `settings.gradle.kts` glob
- `features:more:impl:presentation` — imports `features:nutrition:api:navigation` to route `MenuItemClicked(id = "3")` → `goTo(NutritionScreen)`
- The future `features/item-detail` would also import `features:nutrition:api:navigation`, but that's a downstream spec

**Page-view tracking**: provided automatically by `composeApp/src/commonMain/.../navigation/AnalyticsNavigationListener.kt` — no per-feature wiring.

---

## Testing Notes

7 test artifacts, scoped to **non-network** assertions only. The WebView primitive's content rendering is the WebView spec's coverage concern; nutrition tests verify state→props plumbing.

### Unit tests

| File | Asserts |
|------|---------|
| `impl/presentation/src/commonTest/.../NutritionPresenterTest.kt` | Given fake `GetNutritionContent` emits `NutritionContent(url)` → state.url == url. `BackClicked` event → `navigator.pop()` invoked. |
| `impl/domain/src/commonTest/.../GetNutritionContentImplTest.kt` | Use case forwards repo flow unchanged (single-emission test). |
| `impl/data/src/commonTest/.../NutritionRepositoryImplTest.kt` | Given `AppBuildConfig` with `nutritionUrl = X` → flow emits `NutritionContent(X)`. |

### Fake (test/ module)

```kotlin
// features/nutrition/test/src/commonMain/.../FakeGetNutritionContent.kt
class FakeGetNutritionContent(
    initial: NutritionContent = DEFAULT,
) : GetNutritionContent, /* CenterPostSubjectInteractor base */ {
    fun emit(content: NutritionContent) { /* publish via internal flow */ }
    companion object {
        val DEFAULT = NutritionContent(url = "https://example.test/nutrition")
    }
}
```

`@ContributesBinding(AppScope::class)` for navint-tests DI auto-discovery. No `@Inject`.

### UI Component tests (Android)

| File | Asserts | Does NOT assert |
|------|---------|-----------------|
| `impl/presentation/src/androidDeviceTest/.../NutritionUiTest.kt` | Scaffold renders. Top bar title = `"Nutrition"`. `SCREEN` test tag present. Back-button click fires `BackClicked` event. `WEBVIEW` test tag is present when state.url is non-null. | Anything about WebView content rendering, JS execution, page-load callbacks. |
| `NutritionUiRobot.kt` | Exposes `verifyTopBarTitle()`, `verifyScreenTagPresent()`, `verifyWebViewTagPresent()`, `clickBack()`. | — |
| `NutritionStateRobot.kt` | Builds `NutritionUiState` instances for various test cases (url=null, url=sentinel). | — |

**Sentinel URL**: tests pass `https://example.test/nutrition` (RFC2606 non-routable domain) so the WebView's `loadUrl` doesn't make real network calls.

### UI Component tests (iOS — ViewInspector)

| File | Asserts | Does NOT assert |
|------|---------|-----------------|
| `iosApp/iosAppTests/UIComponent/NutritionViewTest.swift` | SwiftUI tree contains `NutritionView`. `SCREEN` accessibility identifier present. ProgressView shown when `state.url == nil`. WebView placeholder rendered when non-null (presence-only via `WEBVIEW` accessibility id). | WKWebView load events, page rendering, JS sandbox, navigation delegate behavior. |
| `NutritionViewRobot.swift` | Exposes `verifyScreenAccessibilityId()`, `verifyProgressViewVisible()`, `verifyWebViewPresent()`. | — |
| `NutritionStateRobot.swift` | Builds `NutritionUiState` instances for nullable/non-null url cases. | — |

Sentinel URL same as Android: `https://example.test/nutrition`.

### Navigation/Integration test

| File | Asserts |
|------|---------|
| `testing/navint-tests/src/androidDeviceTest/.../NutritionNavigationTest.kt` | (1) Tap "Nutrition" entry on More tab → `NutritionScreen` is presented. (2) Back from `NutritionScreen` → returns to More. |

iOS navint test for the same flow can land in `iosApp/iosAppTests/NavInt/` as a follow-up — the codebase iOS navint tests primarily cover `NavigationStateManager`, not feature-level entry; flag this as a follow-up if iOS navint coverage is expected.

### Test artifact summary

Total new files created by `/add-feature`:
- 3 unit tests (Kotlin) — presenter, use case impl, repository impl
- 1 fake (Kotlin commonMain in `test/` module)
- 3 Android UI component test files (UiTest, UiRobot, StateRobot)
- 3 iOS UI component test files (ViewTest, ViewRobot, StateRobot)
- 1 Android navint test
- **11 test files total**

---

## Constraints & Considerations

- **Hardcoded English strings** — `"Nutrition"` (title) and `"Back"` (content description) are inline in v1. Same localization debt as the WebView primitive itself; swap to string resources when Phrase wires up.
- **No network in `impl/data` v1** — `NutritionRepositoryImpl` only reads from `AppBuildConfig`. The module's `build.gradle.kts` does NOT add `core:network:api` as a dep. When v2 introduces a real backend (per-item data, A/B variants), add the network dep then.
- **WebView primitive coupling** — the feature trusts the WebView primitive's contracts (loading UX, error UX, off-host link handoff, ephemeral cookies, JS opt-in semantics). Behavior changes in the primitive can affect this feature; track via the WebView spec's AGENTS.md.
- **Sentinel URL hygiene** — tests must use `https://example.test/nutrition` (or another RFC2606 reserved domain). Real URLs in tests would attempt network round-trips and flake.
- **iOS navigation asymmetry** — per the WebView primitive's contract, iOS edge-swipe always pops host; there's no "back in WebView" affordance on iOS in v1. The `canGoBack` binding is dead-state on iOS; live on Android via `BackHandler`. Documented in the iOS Presentation AGENTS.md.
- **Page-view tracking dependency** — relies on `composeApp/src/commonMain/.../navigation/AnalyticsNavigationListener.kt` continuing to fire `trackScreenView` on `goTo`. If that listener is ever removed or its behavior changes, nutrition silently loses tracking. AGENTS.md should call this dependency out.
- **No per-market override** — the nutrition destination is a single global reference. If a market-specific one is ever wanted, override `NUTRITION_URL` in that market's `.properties` files (no code changes required).

---

## Out of Scope

- **`features/item-detail` integration** — no item-detail feature exists yet; nutrition will be reachable from an item PDP in a future spec. v1 is reachable only from More.
- **Per-item nutrition pages** — the URL is a single calculator destination; there's no `NutritionScreen(itemId)` parameterization. If a future per-item URL pattern emerges, `NutritionScreen` becomes a `data class` with `itemId: String?`.
- **Offline cache / pre-fetch** — WebView always fetches fresh; no offline support.
- **`nutrition_viewed` per-feature analytics event** — page-view tracking is auto-handled by `AnalyticsNavigationListener`. Custom per-feature events (e.g., scroll-depth, calculator-completion) are not in scope.
- **Feature flag (`nutrition.enabled`)** — nutrition is always on. Disabling requires removing the `NutritionTabExtension` contribution.
- **Native nutrition UI** — v1 embeds the Sample Platter web calculator. A native rendering of nutrition data (charts, filters, allergen badges) would be a separate effort.
- **iOS-specific "back in WebView" UX** — accepted asymmetry per the WebView primitive's documented platform contract.
- **DE / AU per-market URL overrides** — both fall back to US default v1 until DE/AU calculator URLs are validated.
- **iOS navint test for More→Nutrition→Back** — Android navint covers this; iOS navint coverage is a follow-up if the team expects it.
- **Localization of `"Nutrition"` and `"Back"` strings** — hardcoded English v1; swap when Phrase + `Localizable.strings` are wired.

---

## Decisions

Each row logs a grilled question, the chosen answer, and the rationale.

### Resolved

| # | Question | Decision | Rationale |
|---|----------|----------|-----------|
| 1 | API shape (single Screen vs split) | **`data object NutritionScreen : Screen`** — no params. Single URL nutrition site, no per-item branching. | User clarified: "it doesn't take an item id, it is just a nutrition url site - one screen/webview." Removes the `LoginScreen(returnTo:)` precedent argument; simpler is better. |
| 2 | URL resolution strategy | **Direct from `AppBuildConfig.nutritionUrl`**. Repository wraps `flowOf(appBuildConfig.nutritionUrl)` for module symmetry. | User confirmed "build config works." Pure URL — no streaming network state to resolve. Repo wrapper preserves the api/impl boundary so presenters never read AppBuildConfig directly, and slots in a v2 RemoteDataSource later without changing presenter contracts. |
| 3 | Domain model shape | **`data class NutritionContent(val url: String)`** — single field. Top-bar title `"Nutrition"` is a UI-layer constant, not domain data. | Title is UI copy (will localize via string resources, not the model). Convention says every feature has a `*Content` data class; matches `RecentsContent`/`LoginContent` minimal-model precedent. |
| 4 | Data source (network or none) | **No network v1.** No `RemoteDataSource`, no DTO, no mapper, no Ktor client. `NutritionRepositoryImpl` reads from `AppBuildConfig` only. | URL is compile-time market config, not server-driven. v2 can add a RemoteDataSource behind the existing repository interface without breaking presenter contracts. |
| 5 | Loading/error UX layering | **Feature owns scaffold; WebView owns body.** Scaffold: TopAppBar with `"Nutrition"` title + back chevron. Body: `WebViewContent` (Android) / `WebView` (iOS). When `state.url == null`: blank `Box` (Android) / centered `ProgressView` (iOS). | No double-loading-state. WebView primitive's loading/error UX is sufficient for the body. iOS uses ProgressView matching `RecentsView` Loading pattern; Android uses blank Box because the URL flow is synchronous from build-config. |
| 6 | More tab entry shape | **Use the existing hardcoded `MoreMenuItem(id = "3", icon = "🥗", title = "Nutrition")`** in `MoreRepositoryImpl`. `MorePresenter` routes id `"3"` → `goTo(NutritionScreen)`, same pattern as id `"1"` → `RecentsScreen`. | The Nutrition entry already exists in the More menu — adding a `NutritionTabExtension` would duplicate it. The `MoreTabExtension` Metro multibinding is for features that need to inject themselves dynamically (debug-only, plugin-style); permanent core entries live in the hardcoded menu list. User correction during scaffolding. |
| 7 | iOS scaffold | **Standard `NavigationStack` chevron**, no custom toolbar back button. `canGoBack` binding declared but unread (iOS edge-swipe always pops host per WebView primitive contract). `ProgressView` shown when `state.url == nil`. JS opted in. `samplePlatterColors` env, `accessibilityIdentifier(tags.SCREEN)`/`tags.WEBVIEW`. | Matches `RecentsView` pattern. Doesn't fight platform-native back gesture semantics. iOS Compose-MP not introduced. |
| 8 | Analytics integration | **Nothing to wire.** `AnalyticsNavigationListener` in `composeApp` already fires `trackScreenView("NutritionScreen")` on every `goTo`. No per-feature analytics code. | Discovered the existing nav-level listener after the user pointed out "the analytics other screens also already have are fine, should be built into the navigation." Page-view tracking is a navigation concern, not a feature concern; nutrition gets it free. |
| 9 | Test scope | **7 artifacts, non-network only.** Tests verify state→props plumbing using sentinel URL (`https://example.test/nutrition`); explicitly do NOT assert WebView content rendering, JS execution, or page-load callbacks. | The WebView primitive's content behavior is the WebView spec's coverage concern. Hermetic tests can't (and shouldn't) hit `spoonacular.com`. Sentinel URL avoids real network round-trips. |
| 10 | Feature flag | **No flag.** Nutrition is always on. | User pushback: "if using build config what about this makes you think we need a feature flag? there is no feature flag it is always turned on." Build-config supplies the URL; the feature is simply present. KillSwitch on a low-risk public-info surface is feature-flag cargo-cult. |

### Deferred

None. All grills resolved without deferral.

---

## Open Questions / Follow-ups (not blocking this spec)

These are flagged for future tracking, not for this implementation:

- **`features/item-detail`** — when this lands, it may want to navigate to nutrition with an item context. That spec will decide whether to extend `NutritionScreen` to a `data class` with optional `itemId`, or introduce a separate `NutritionItemScreen`.
- **DE / AU per-market URL overrides** — both currently fall back to the US default. When localized calculator URLs are validated, override the per-market `.properties` files. No code changes required — purely build-config edits.
- **Localization** — `"Nutrition"`, `"Back"` content descriptions, etc. all hardcoded English v1. Swap to resource lookups when Phrase + `Localizable.strings` are wired.
- **iOS navint test for More→Nutrition→Back** — Android navint covers this; iOS coverage is a follow-up if the team expects it.

---

## Original Requirements

> **Source**: design discussion (inline conversation between user and assistant)
> **Converted on**: 2026-04-29
> **Spec type**: `new` (explicit — no existing `features/nutrition` module)

<details>
<summary>Original acceptance criteria (click to expand)</summary>

```
Add a `features/nutrition` feature module — the first consumer of the WebView primitive
that just landed in `core:presentation`.

Background:
- The WebView primitive (WebViewContent on Android, WebView SwiftUI on iOS) is now in
  place. Spec at specs/core-presentation-webview.md. It's a low-level UI primitive —
  features that want to render web content own their own destination semantics
  (typed Screen, URL resolution, analytics).

`features/nutrition` is the first feature to consume it. The bar for "earn a feature
module" was clearly cleared in the prior discussion:
- Multi-market URL resolution
- Linkable from `item-detail` (future feature) with typed NutritionScreen(itemId: String?)
  parameter   [SUPERSEDED BY GRILL Q1 — user clarified no item ID, single URL site]
- Future expansion potential: offline-cached fallback, per-item highlights,
  "view full menu" CTA

Scope (in scope):
- New features/nutrition/ module set with the standard 6-submodule split
- NutritionScreen(itemId: String?) — Circuit Screen, data class
  [SUPERSEDED BY GRILL Q1 — collapsed to data object, no params]
- NutritionPresenter resolves the URL via a NutritionRepository (which uses build-config
  per-market base URL + item ID query param)
  [REVISED: build-config supplies the full URL; no item ID concatenation]
- Renders by hosting WebViewContent (Android) / WebView SwiftUI view (iOS) inside a
  feature-owned scaffold (top app bar with back button, item title from state)
- JS opted in (allowJs = true) since nutrition portals typically have interactive
  filters/charts
- Hooks into core:build-config for the per-market nutrition base URL — likely a new
  field, e.g., nutritionBaseUrl  [REVISED: nutritionUrl, full URL, not a base]
- Where this feature is reachable from in v1: contributed entry on the More tab via
  MoreTabExtension — same pattern as features/debug-menu

Open decisions to grill:
1. API shape  [RESOLVED Q1 — single data object NutritionScreen]
2. URL resolution strategy  [RESOLVED Q2 — direct from AppBuildConfig, repository wraps]
3. Domain model shape  [RESOLVED Q3 — NutritionContent(url: String)]
4. Data source  [RESOLVED Q4 — no network v1, build-config only]
5. Loading/error UX  [RESOLVED Q5 — feature owns scaffold, WebView owns body]
6. More tab entry  [RESOLVED Q6 — 🥗 + Nutrition + isDebugOnly=false]
7. iOS scaffold  [RESOLVED Q7 — standard NavigationStack, no custom back, ProgressView during null]
8. Analytics  [RESOLVED Q8 — auto-tracked via AnalyticsNavigationListener, no feature code]
9. Tests  [RESOLVED Q9 — 7 artifacts, sentinel URL, non-network assertions only]
10. Feature flag  [RESOLVED Q10 — no flag, always on]
```

</details>
