# Spec — Add WebView primitive to `core:presentation`

> **Spec type**: `change` (modifies existing `core:presentation` module + establishes new iOS convention folder)
> **Downstream skill**: `/update` (Kotlin side) + manual SwiftUI work for iOS side
> **Status**: ready for implementation (all 9 grill questions resolved)

---

## Overview

Add a reusable, embeddable WebView primitive to `core:presentation` so multiple features (nutrition, legal/T&C/PP, OSS licenses, FAQ articles) can render web content inline without each reimplementing WebView setup, link handling, loading state, and error UX. The primitive is split-paradigm — an Android Compose component + an iOS SwiftUI component — matching the codebase's existing Android-Compose / iOS-SwiftUI architecture rather than introducing Compose-MP-iOS rendering for one primitive.

**Feature**: `core:presentation` (Kotlin) + new `iosApp/iosApp/Presentation/` folder (iOS sibling)
**Change type**: enhancement
**Scope**: cross-layer — extends a core module's source sets, adds a new top-level iOS folder, updates two AGENTS.md files

---

## Current Behavior

**What the user sees**: nothing — `core:presentation` today only contains Compose extension functions (`rememberFlag`, `rememberConfig`, `rememberCenterPost`, `collectAsState`). No UI primitives.

**What the code does**:
- `core:presentation` ships `commonMain`-only Compose code: `@Composable` extension functions over Compose-free core APIs (`core:centerpost`, `core:remote-config:api`).
- Build script pulls only `compose.runtime` — no `compose.foundation`, no `compose.material3`, no `compose.ui`. The module is "Compose-runtime-aware, UI-rendering-free."
- Convention plugin `mockdonalds.kmp.presentation` auto-wires `core:presentation` into every feature `impl/presentation` `commonMain`. Feature consumers pick up extensions automatically.
- `ComposeIsolationTest` allows Compose imports under `core:presentation` (any path).
- iOS uses SwiftUI for all rendering (`iosApp/iosApp/Features/{Home,Login,Order,...}`) — no Compose-MP-iOS UI today.
- No iOS-side equivalent location for cross-cutting SwiftUI primitives. Each feature owns its own SwiftUI folder under `iosApp/iosApp/Features/{Name}/`.

**Relevant files**:
- `core/presentation/build.gradle.kts`
- `core/presentation/AGENTS.md`
- `core/presentation/src/commonMain/kotlin/com/mockdonalds/app/core/presentation/{centerpost,remoteconfig}/`
- `iosApp/iosApp/` (no `Presentation/` folder yet)
- `testing/architecture-check/src/test/kotlin/.../ComposeIsolationTest.kt`

---

## Desired Behavior

**What the user sees**: still nothing directly — this primitive is consumed by future features. After landing, downstream consumers can render web content with two boilerplate calls:

```kotlin
// Android consumer
val state = rememberWebViewState()
BackHandler(enabled = state.canGoBack) { state.goBack() }
WebViewContent(url = "https://nutrition.mockdonalds.com/big-mac", state = state)
```

```swift
// iOS consumer
@State private var canGoBack = false
WebView(url: URL(string: "https://...")!, canGoBack: $canGoBack)
```

**What the code should do**:
- `core:presentation` gains an `androidMain` source set with a `webview/` subpackage exposing `WebViewContent`, `WebViewState`, `rememberWebViewState`, `WebViewError`.
- Android primitive wraps `android.webkit.WebView` in `AndroidView`, owns its loading + error UX, defaults to JS off and ephemeral cookies, hands off off-host links to Custom Tabs.
- New `iosApp/iosApp/Presentation/` top-level folder (sibling to `Features/`, `Akamai/`, `Circuit/`, `Harness/`, `Theme/`) holds `WebView/WebView.swift` — a SwiftUI `UIViewRepresentable` over `WKWebView` with the same conceptual API.
- Both platforms expose internal-history state to the host so the host wires its own back handling.

---

## Affected Layers

- [x] **core module(s)** — `core:presentation` gains an `androidMain` source set + new `webview/` subpackage
- [x] **iosApp (SwiftUI)** — new `iosApp/iosApp/Presentation/` folder + `WebView/WebView.swift`
- [ ] api/domain — n/a
- [ ] api/navigation — n/a
- [ ] impl/domain — n/a
- [ ] impl/data — n/a
- [ ] impl/presentation (common) — n/a (consumers will use this in their androidMain UI files; that's a future spec)
- [ ] impl/presentation (androidMain) — n/a (downstream consumer concern)
- [ ] impl/presentation (iosMain) — n/a
- [ ] test/ — n/a (no fakes — this is UI infrastructure, exercised via UI Component tests in consuming features)
- [ ] build-config — n/a
- [ ] composeApp — n/a (auto-wired via convention plugin)

---

## Domain Model Changes

n/a — this primitive has no domain models. The new public types are all UI-layer:

```
core:presentation/androidMain/.../webview/

WebViewError                            // surfaced by onError + errorContent
  ├── url: String                       // URL that failed
  ├── code: Int                         // platform-specific code (Android: WebViewClient.ERROR_*)
  └── description: String               // human-readable description from WebView

WebViewState                            // @Stable holder, exposed via rememberWebViewState()
  ├── canGoBack: Boolean by mutableStateOf(false)   // internal set; reads true when WebView has back history
  └── goBack(): Unit                                // calls webView.goBack() if canGoBack
```

```
iosApp/iosApp/Presentation/WebView/

WebViewError (Swift struct)
  ├── url: URL
  ├── code: Int
  └── description: String
```

---

## API / Network Changes

n/a — this is a UI primitive, no HTTP/DTO/mapper changes.

---

## Use Case Changes

n/a — no domain or use cases involved.

---

## UI / Presenter Changes

### New public surface in `core:presentation` (`androidMain`)

#### `WebViewContent` Composable

```kotlin
@Composable
fun WebViewContent(
    url: String,
    modifier: Modifier = Modifier,
    allowJs: Boolean = false,
    onExternalLink: ((String) -> Boolean)? = null,
    onError: ((WebViewError) -> Unit)? = null,
    state: WebViewState = rememberWebViewState(),
    errorContent: (@Composable (WebViewError, retry: () -> Unit) -> Unit)? = null,
)
```

**Behavior**:
- Renders an `AndroidView { WebView }` filling the modifier-supplied bounds.
- Sets `WebSettings.javaScriptEnabled = allowJs` (default false).
- Configures a `WebViewClient` that:
  - Updates `state.canGoBack` after every navigation finishes.
  - Surfaces terminal navigation failures (not sub-resource failures) via `onError`.
  - Routes off-host requests through `onExternalLink` if provided; if null, opens off-host requests in **Android Custom Tabs** (`androidx.browser:browser`) and returns `true` to cancel in-frame load.
  - "Off-host" = different scheme OR different host than the originally-loaded `url`. `mailto:`, `tel:`, custom schemes all count as off-host.
- During an in-flight navigation, displays a `LinearProgressIndicator` anchored at the top edge of the WebView. Hidden when load completes.
- On terminal error, replaces WebView content with `errorContent(error, retry)` if provided, otherwise the built-in error view.
- On disposal, clears cookies for the loaded host (`CookieManager` best-effort) — see Constraints for the Android caveat.

#### `WebViewState` + `rememberWebViewState`

```kotlin
@Stable
class WebViewState internal constructor() {
    var canGoBack: Boolean by mutableStateOf(false)
        internal set
    internal var goBack: () -> Unit = {}
}

@Composable
fun rememberWebViewState(): WebViewState = remember { WebViewState() }
```

The state holder is wired by `WebViewContent` so the host can `BackHandler(enabled = state.canGoBack) { state.goBack() }` without coupling to the WebView's internals.

#### `WebViewError`

```kotlin
data class WebViewError(
    val url: String,
    val code: Int,
    val description: String,
)
```

#### Built-in error view (internal Composable)

Centered column inside the WebView's bounds:
- Outlined error icon (`Icons.Outlined.ErrorOutline`)
- Title: `"Couldn't load this page"` (hardcoded English — see Constraints)
- Description: `error.description` (truncated if needed)
- Primary `Button` labeled `"Try again"` calling `retry`

### iOS public surface (`iosApp/iosApp/Presentation/WebView/WebView.swift`)

```swift
struct WebView: UIViewRepresentable {
    let url: URL
    var allowJs: Bool = false
    var onExternalLink: ((URL) -> Bool)? = nil
    var onError: ((WebViewError) -> Void)? = nil
    @Binding var canGoBack: Bool
    var errorView: ((WebViewError, _ retry: @escaping () -> Void) -> AnyView)? = nil
    
    // Coordinator handles WKNavigationDelegate, exposes goBack() to parent via binding semantics
}

struct WebViewError {
    let url: URL
    let code: Int
    let description: String
}
```

**Behavior**:
- Wraps `WKWebView` with `WKWebViewConfiguration.websiteDataStore = .nonPersistent()` (always ephemeral).
- `WKWebpagePreferences.allowsContentJavaScript = allowJs` per navigation, set in `decidePolicyFor`.
- `WKNavigationDelegate.decidePolicyFor`:
  - If request URL host == initial URL host → `.allow`.
  - Else → call `onExternalLink(url)`. If result is `true`, return `.cancel`. If `false` or callback is `nil`, default behavior: `UIApplication.shared.open(url)` then `.cancel`.
- Updates `canGoBack` binding after each navigation completes.
- Surfaces terminal errors via `onError`; renders `errorView` (or built-in default) over the WebView.
- Loading state: SwiftUI `ProgressView()` linear-style, anchored top.

### Built-in iOS error view

Symmetric to Android — vertical stack: SF Symbol icon (`exclamationmark.triangle`), `"Couldn't load this page"` title, error description, primary `Button("Try again")`. Uses `iosApp/iosApp/Theme/` colors.

---

## Navigation Changes

n/a — this primitive has no navigation surface. Hosts that *use* it (future `nutrition`, `legal`, etc. features) will define their own `Screen` types and route to them; that's a downstream spec.

---

## Analytics Changes

n/a — `core:analytics` integration deferred. If/when needed (e.g., page-load tracking, link-click events), it adds via opt-in callback params. Not in v1.

---

## Feature Flag Changes

n/a — no flags introduced.

---

## Build Config Changes

n/a — no config fields.

### Build script changes (informational, not BuildConfig fields)

`core/presentation/build.gradle.kts` gains an `androidMain` source set:

```kotlin
sourceSets {
    commonMain.dependencies {
        api(project(":core:centerpost"))
        api(project(":core:remote-config:api"))
        implementation(compose.runtime)
    }
    androidMain.dependencies {
        implementation(compose.foundation)
        implementation(compose.ui)
        implementation(compose.material3)
        implementation(compose.materialIconsExtended)
        implementation(libs.androidx.browser)   // NEW: Custom Tabs for off-host link handoff
    }
}
```

`libs.versions.toml` gains:
```toml
androidx-browser = "1.8.0"   # latest stable; pin to current version when implementing

androidx-browser = { group = "androidx.browser", name = "browser", version.ref = "androidx-browser" }
```

### `iosApp/iosApp.xcodeproj` changes

- New folder reference: `iosApp/iosApp/Presentation/`
- New file membership: `iosApp/iosApp/Presentation/WebView/WebView.swift` added to the `iosApp` target.
- New file: `iosApp/iosApp/Presentation/AGENTS.md` (not added to target — markdown).

---

## Test Impact

### New Test Scenarios

#### `core:presentation` (Android)

The convention plugin gives `core:presentation` an `androidDeviceTest` source set already (`mockdonalds.kmp.presentation` plugin), but `core:presentation` itself doesn't apply that plugin — it uses `mockdonalds.kmp.library` + `org.jetbrains.compose` directly. Adding UI tests to `core:presentation` would require either:

1. Applying `mockdonalds.kmp.presentation` to `core:presentation` (likely circular — that plugin auto-wires `core:presentation` into itself).
2. Manually wiring `androidDeviceTest` source set in `core:presentation/build.gradle.kts`.
3. Skipping UI tests at this level, exercising the primitive only via consumer features' UI Component tests.

**Recommendation**: option 3 — defer UI Component tests to the first downstream consumer (e.g., the `nutrition` feature spec). The primitive is small and its behavior is exercised end-to-end the moment any consumer renders it. This also matches how `core:theme` is tested today (no module-level UI tests; tested via consuming features).

- [ ] No new tests at the `core:presentation` level. Behavior is exercised when `nutrition` (or first consumer) lands its UI Component tests.

#### iOS

- [ ] No standalone tests. iOS UI Component tests already use ViewInspector against feature views; the SwiftUI `WebView` will be exercised through the first consumer feature's `UIComponentTests` test plan target.

#### Manual smoke verification (one-time during implementation)

- [ ] Android: throwaway demo screen renders `WebViewContent("https://example.com")` — verify load, JS-off behavior, off-host link → Custom Tabs handoff, error retry.
- [ ] iOS: throwaway SwiftUI preview renders `WebView(url: URL(string: "https://example.com")!)` — same checklist.
- [ ] Confirm `BackHandler` round-trip: navigate to a link inside a host site, system back goes "back in WebView" once, then pops host on second press.

### Changed Tests

- [ ] `ComposeIsolationTest` — verify still passes after `androidMain` Compose UI lands in `core:presentation`. (Should pass — the test allows `core:presentation` for any path.)

### Fakes to Update

- [ ] None — no abstractions, no fakes.

---

## Documentation Changes

### `core/presentation/AGENTS.md`

**Charter loosen** — replace:

> Cross-cutting Compose-specific helpers and extensions over otherwise Compose-free core modules.

with:

> Cross-cutting Compose-specific code: extensions over otherwise Compose-free core modules **and standalone UI primitives that don't belong in any single feature**.

**Rules section** — replace:

> No Metro DI annotations here — this is **extension functions only**. No Circuit either.

with:

> No Metro DI annotations here — this module is **Compose extensions and UI primitives only**. No Circuit either; presenter wiring stays in feature presentation modules.

**Architecture diagram** — extend with:

```
core/presentation/
  src/commonMain/kotlin/.../core/presentation/
    centerpost/    (existing)
    remoteconfig/  (existing)
  src/androidMain/kotlin/.../core/presentation/
    webview/                                      NEW
      WebViewContent.kt                            @Composable, AndroidView { WebView } wrapper
      WebViewState.kt                              @Stable holder + rememberWebViewState()
      WebViewError.kt                              data class
      internal/                                    package-private
        WebViewLoadingIndicator.kt                 LinearProgressIndicator overlay
        WebViewErrorView.kt                        default error layout
        WebViewClientFactory.kt                    builds WebViewClient with off-host handoff
```

**Public API table** — append four rows for `WebViewContent`, `rememberWebViewState`, `WebViewState`, `WebViewError` (note them as `androidMain` only).

**Wiring section** — note that `core:presentation`'s `androidMain` deps are not auto-wired into consumers; the `mockdonalds.kmp.presentation` convention plugin already pulls Compose into feature androidMain, so `WebViewContent` is callable with no extra dep declaration.

### `iosApp/iosApp/Presentation/AGENTS.md` (NEW)

```markdown
# iosApp/iosApp/Presentation

## Purpose

iOS sibling to Kotlin `core:presentation`. Houses cross-cutting SwiftUI primitives 
that don't belong inside a single feature — WebView, future shared image loaders, 
shared loading/error states.

## Conventions

- One subfolder per primitive concept (`WebView/`, `Loading/`, etc.).
- Primitives are pure SwiftUI views — no business logic, no domain types, no KMP 
  imports beyond shared models passed in by the consumer.
- New files must be added to the `iosApp` target via `iosApp.xcodeproj` membership.
- Naming mirrors the Kotlin side where it makes sense (`WebView` matches 
  `WebViewContent` conceptually but adopts SwiftUI idioms in API shape).

## Current contents

| Folder | Purpose |
|--------|---------|
| `WebView/` | `UIViewRepresentable` over `WKWebView` for rendering web content inline (T&C, PP, nutrition, FAQ articles). |
```

---

## Constraints & Considerations

- **Hardcoded English strings (v1)** — `"Couldn't load this page"` and `"Try again"` are hardcoded inline. `core:strings` is a placeholder today (Phrase integration pending) and iOS has no `Localizable.strings` wired. When localization lands, swap the inline strings for resource lookups. Track as known debt.
- **Android cookie clearing is best-effort** — `android.webkit.CookieManager` is a global singleton; there's no per-WebView-instance cookie jar. The implementation will call `CookieManager.getInstance().removeAllCookies(null)` (or per-host equivalent) on `onDispose` of the Composable, which clears cookies *for the entire app's WebViews*. This is a known Android-platform limitation. iOS uses `.nonPersistent()` which is properly per-instance.
- **iOS asymmetry on back navigation** — SwiftUI's `NavigationStack` edge-swipe always pops the host stack; there's no equivalent of Android's `BackHandler`. Hosts must use a toolbar back button (or similar) to expose "back in WebView" when `canGoBack == true`. Document this asymmetry in consumer feature specs.
- **No Compose-MP-iOS introduction** — this spec deliberately keeps iOS on SwiftUI. A future decision to render Compose UI on iOS would be a separate, larger architectural change.
- **Off-host detection is host-only** — initial-host comparison covers `nutrition.mockdonalds.com` → `tracker.mockdonalds.com` correctly (different host, treated off-host). Subdomain matching is *not* fuzzy — if you want subdomains of a parent zone treated as in-frame, pass an `onExternalLink` callback that implements that policy.
- **Authenticated WebView content is unsupported** — ephemeral cookies mean session tokens won't persist. If/when an authenticated WebView consumer lands (account portal, deletion flow), revisit by adding `persistentSession: Boolean = false` param. Non-breaking change, defer until needed.
- **Custom Tabs availability on Android** — older Android devices without Chrome / a Custom-Tabs-capable browser fall back to `Intent.ACTION_VIEW`. The `androidx.browser:browser` lib handles this fallback automatically.

---

## Out of Scope

- **Circuit `WebViewScreen`** — no Circuit screen wrapping the primitive at this layer. Consumer features own their own destinations and Presenters. If 2+ features eventually need an identical generic Circuit destination (`features/web-content`), that's a future spec.
- **`features/nutrition`** — separate spec. Will be the first consumer of this primitive once it lands.
- **Generic `features/web-content` destination** — deferred until 2+ features need an identical "render arbitrary URL" Circuit screen.
- **Compose Multiplatform iOS** — explicitly NOT introduced. iOS stays on SwiftUI per the codebase's existing architecture.
- **Localization of error strings** — hardcoded English in v1. Localization swap is a downstream change once Phrase is wired and `Localizable.strings` exists.
- **Authenticated/persistent-session WebView mode** — no `persistentSession` param in v1. Add when first authenticated consumer lands.
- **Analytics integration** — no page-load or link-click tracking events in v1. Opt-in callbacks can be added when `core:analytics` integration is needed.
- **Standalone UI Component tests at `core:presentation` level** — coverage deferred to first downstream consumer feature's `UIComponentTests`.
- **Konsist/Harmonize tests for the new charter** — none added; existing build-graph and `ComposeIsolationTest` enforcement is sufficient. AGENTS.md update is the doc-level guardrail.

---

## Decisions

Each row logs a grilled question, the chosen answer, and the rationale.

### Resolved

| # | Question | Decision | Rationale |
|---|----------|----------|-----------|
| 1 | iOS implementation strategy | **Split-paradigm**: Android Compose primitive in `core:presentation/androidMain`; iOS SwiftUI primitive in new `iosApp/iosApp/Presentation/WebView/`. No Compose-MP-iOS. | Codebase reality: `core:presentation` is `commonMain`-only with `compose.runtime` (no UI libs); iOS is 100% SwiftUI; no other module renders Compose UI on iOS. Splitting matches the existing architecture exactly and avoids introducing Compose-MP-iOS infra for one primitive. |
| 2 | iOS WebView location | `iosApp/iosApp/Presentation/WebView/WebView.swift` with folder-level AGENTS.md. | Mirrors existing top-level convention `Akamai/`, `Circuit/`, `Harness/` (each is iOS sibling to a `core:` Kotlin module). Establishes the same pattern for `core:presentation`. |
| 3 | Public API shape | **Android**: `WebViewContent(url, modifier, allowJs, onExternalLink, onError, state, errorContent)` + `WebViewState` + `rememberWebViewState` + `WebViewError`. **iOS**: `WebView(url, allowJs, onExternalLink, onError, canGoBack: Binding, errorView)`. Title param dropped. | Title belongs in the consumer's `TopAppBar` / `.navigationTitle`, not the primitive. State exposure is required for Q8 back-nav wiring. Slot params (`errorContent`, `errorView`) cover localization and customization without bloating defaults. |
| 4 | JavaScript default | **Off** by default. Per-call opt-in via `allowJs: Boolean = false`. | Most v1 consumers (T&C, PP, OSS Licenses, FAQ) are static HTML and don't need JS. Off-by-default reduces XSS surface. Nutrition opts in. Auditable via grep. |
| 5 | External link handling | **Default-deny with callback override**: off-host links go to system browser (Android Custom Tabs / iOS `UIApplication.openURL`) automatically. Consumer can override via `onExternalLink` returning a Bool. | "Tap a Twitter link, don't render Twitter inside SamplePlatter" matches user expectations for embedded WebViews. Avoids per-consumer boilerplate. Override callback handles edge cases (e.g., subdomain allowlists, custom schemes). Adds `androidx.browser:browser` dep — single new lib, ~50KB. |
| 6 | Loading & error UX | **Loading**: `LinearProgressIndicator` on Android, linear `ProgressView()` on iOS. Both anchored top. **Error**: built-in centered view (icon + message + retry button); override slots `errorContent` / `errorView` for customization. **Strings**: hardcoded English in v1. Surface only terminal navigation errors (not sub-resource failures). | Standard mobile WebView pattern. Hardcoded strings dodge the not-yet-wired Phrase/Localizable infra. |
| 7 | Cookies & cache policy | **Always ephemeral** in v1; no persistence param. iOS uses `.nonPersistent()` (clean per-instance); Android clears cookies on `onDispose` (best-effort due to `CookieManager`'s global singleton). | All v1 consumers are public unauthenticated content (T&C, PP, OSS, FAQ, nutrition info). YAGNI — adding `persistentSession: Boolean = false` later is non-breaking. Privacy-safer default. |
| 8 | Back navigation | **Primitive exposes state, host wires the back press.** Android: `WebViewState.canGoBack` + `state.goBack()`, host calls `BackHandler(enabled = state.canGoBack) { state.goBack() }`. iOS: `@Binding canGoBack`, host wires a toolbar back button. iOS edge-swipe always pops host (platform-native). | Keeps primitive UI-only and decoupled from system navigation. Lets hosts compose with confirm-on-back, conditional back, etc. Same boilerplate as every WebView-hosting screen. iOS asymmetry is a platform contract, not worth fighting. |
| 9 | AGENTS.md update & enforcement | **Doc updates only.** Loosen `core:presentation/AGENTS.md` charter to allow standalone UI primitives + add public API rows + new androidMain deps. Create new `iosApp/iosApp/Presentation/AGENTS.md`. **No new Konsist/Harmonize tests.** | `ComposeIsolationTest` already allows `core:presentation`. "No Metro/Circuit" is enforced by absence in build.gradle.kts (compile-time guard). Adding tests for absence rules is overhead for a guardrail already covered. |

### Deferred

None. All grills resolved without deferral.

---

## Open Questions / Follow-ups (not blocking this spec)

These are flagged for future tracking, not for this implementation:

- **`features/nutrition` spec** — first consumer of this primitive. Will define `NutritionScreen(itemId)`, URL resolution per market, and (optionally) item-context UI surrounding the WebView.
- **Localization wiring** — once Phrase + `Localizable.strings` are real, swap the WebView's hardcoded English defaults to resource lookups.

---

## Original Requirements

> **Source**: design discussion (inline conversation between user and assistant)
> **Converted on**: 2026-04-29
> **Spec type**: `change` (explicit — `core:presentation` already exists)

<details>
<summary>Original acceptance criteria (click to expand)</summary>

```
Add a reusable WebView Compose primitive to the existing `core:presentation` module 
so features (nutrition, legal/T&C/PP, help articles) can render web content inline.

Scope:
- Add webview/WebViewContent.kt to core:presentation, alongside existing centerpost/ 
  and remoteconfig/ subpackages.
- Platform implementation via expect/actual — Android wraps WebView in AndroidView; 
  iOS wraps WKWebView in UIKitView (Compose Multiplatform iOS, no Swift bridge).
  [SUPERSEDED BY GRILL Q1 — codebase reality check rejected Compose-MP-iOS]
- Update core:presentation/AGENTS.md to expand its charter: it currently allows only 
  "Compose extensions over Compose-free core modules"; this change relaxes that to 
  also allow standalone Compose primitives that don't belong in any source core module. 
  Still no Metro, still no Circuit.

Public API (proposed):
- WebViewContent(url, title?, allowJs = false, onError, modifier) Composable.
  [TITLE PARAM DROPPED PER GRILL Q3]
- Owns its own loading shimmer + error/retry surface so consumers don't reinvent it.
  [SHIMMER → LINEAR PROGRESS INDICATOR PER GRILL Q6]

Open decisions to grill:
1. iOS impl — expect/actual Composable using UIKitView { WKWebView }. Confirm vs 
   SwiftUI bridge.  [RESOLVED Q1 — split-paradigm, SwiftUI on iOS]
2. JS default — off. Nutrition opts in.  [RESOLVED Q4]
3. External links — non-allowlisted domains punt to system browser.  [RESOLVED Q5]
4. Loading/error UX — owned by WebViewContent.  [RESOLVED Q6]
5. Cookies/cache — ephemeral by default; opt-in persistence via param.  [RESOLVED Q7 
   — ephemeral always in v1, no persistence param]
6. Back behavior — internal WebView history pop first, then host pops.  [RESOLVED Q8 
   — state-exposing pattern, host wires BackHandler]
7. Konsist/Harmonize — AGENTS.md update needs matching test/lint check or just doc?
   [RESOLVED Q9 — doc only]

Out of scope for this spec:
- Any Circuit screen wrapping the primitive (no WebViewScreen).
- The features/nutrition feature itself (separate spec).
- Any generic features/web-content reusable destination.

Downstream skill: /update (modifying existing core:presentation module).

Codebase context:
- core:presentation AGENTS.md is at /Users/jkjamies/GitHub/SamplePlatter/core/presentation/AGENTS.md.
- Convention plugin mockdonalds.kmp.presentation already auto-wires this module into 
  every feature impl/presentation.
- Compose isolation is enforced by ComposeIsolationTest in testing:architecture-check.
- Compose Multiplatform 1.10.3 + Kotlin 2.3.20.
```

</details>
