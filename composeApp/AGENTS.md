# composeApp Module

## Purpose

Application shell that wires all feature modules together. Contains platform entry points, navigation infrastructure, and the Metro DI graph. No business logic belongs here.

## Key Files

### commonMain

| File | Purpose |
|------|---------|
| `AppGraph.kt` | `ProdAppGraph` — extends `AppGraph` (from `core:metro`) with `@DependencyGraph(AppScope)`. The `AppGraph` interface and `CircuitProviders` live in `core:metro` and `core:circuit` respectively. |
| `navigation/DeepLinkParser.kt` | Parses URI paths into Screen lists. `findTabByTag()` resolves tab screens. `createDeepLinkParser()` registers all tab screens plus profile/login routes. |
| `navigation/NavigationInterceptor.kt` | `NavigationInterceptor` interface with `interceptGoTo`/`interceptResetRoot` returning `InterceptResult` (Skip or Rewrite). |
| `navigation/AuthInterceptor.kt` | Redirects `ProtectedScreen` navigation to `LoginScreen` when user is not authenticated. |
| `navigation/InterceptingNavigator.kt` | Wraps Circuit's `Navigator`, applies interceptor chain to `goTo`/`resetRoot`, notifies event listeners after successful navigation. Also provides `deepLink()` to intercept a list of screens. |
| `navigation/NavigationEventListener.kt` | Pure observer interface for navigation events. Unlike `NavigationInterceptor` which can rewrite, listeners only observe — no return value. |
| `navigation/AnalyticsNavigationListener.kt` | Tracks screen views via `AnalyticsDispatcher` on `goTo` and `resetRoot`. |

### androidMain

| File | Purpose |
|------|---------|
| `App.kt` | `MockDonaldsApp` composable -- creates `ProdAppGraph`, sets up `rememberSaveableBackStack(root = HomeScreen)`, wires `InterceptingNavigator` with `AuthInterceptor`, handles deep link intents, renders `NavigableCircuitContent` with gesture navigation and `MockDonaldsBottomNavigation`. |
| `MockDonaldsBottomNavigation.kt` | Custom bottom nav bar with glass effect. Uses `TabScreen.tag` for route matching. |
| `MockDonaldsIcons.kt` | Custom `ImageVector` icons for bottom navigation tabs. |

### iosMain (KMP-to-Swift Bridge)

| File | Purpose |
|------|---------|
| `bridge/IosApp.kt` | iOS entry point. Creates `AppGraph`, `BridgeNavigator`, `InterceptingNavigator` with `AuthInterceptor`. Exposes `presenterBridge(screen)` for Swift and `deepLink(uri)` with tab-aware routing. |
| `bridge/BridgeNavigator.kt` | iOS `Navigator` implementation using `Channel<List<NavigationAction>>`. Batches synchronous navigator calls via `dispatch_async(dispatch_get_main_queue())` to avoid SwiftUI animation artifacts. Detects `FlowScreen` in `goTo()` and emits `PresentFlow` instead of `GoTo`. Accepts optional `onSwitchTab` callback for analytics. `notifyTabSelected(tag)` is called from Swift on user tab taps; `suppressTabCallback` flag prevents double-tracking on deep-link-initiated tab switches. |
| `bridge/NavigationAction.kt` | Sealed class: GoTo, Pop, ResetRoot, SwitchTab, DeepLink, PresentFlow, DismissFlow. Actions are batched into lists for single-frame SwiftUI updates. |
| `bridge/CircuitPresenterKotlinBridge.kt` | Bridges Circuit presenters to `StateFlow` via Molecule (`launchMolecule`). Annotated with `@NativeCoroutinesState` for Swift async observation. |

## Auto-Discovery in build.gradle.kts

The `build.gradle.kts` auto-discovers feature modules by scanning the `features/` directory:

1. **commonMain dependencies** -- For each feature, adds `api(:features:<name>:api:domain)`, `api(:features:<name>:api:navigation)`, `implementation(:features:<name>:impl:data)`, `implementation(:features:<name>:impl:domain)`, `api(:features:<name>:impl:presentation)`
2. **iOS framework exports** -- Exports `api:domain`, `api:navigation`, and `impl:presentation` for each feature so Swift can access Screen objects, UiState types, and Event classes
3. **Core exports** -- Exports `:core:circuit` for shared Circuit types (Screen, TabScreen, ProtectedScreen)

Adding a new feature module to `features/` automatically wires it into the app -- no manual dependency edits needed.

## Navigation Flow

1. Android: `MockDonaldsApp` -> `rememberSaveableBackStack` -> `InterceptingNavigator` -> `NavigableCircuitContent`
2. iOS: `IosApp` -> `BridgeNavigator` -> Channel -> Swift `CircuitNavigator` observes actions -> SwiftUI TabView/NavigationStack
3. Deep links: URI -> `DeepLinkParser.parse()` -> `InterceptingNavigator.deepLink()` (applies auth interceptor) -> push screens
4. Auth guard: `ProtectedScreen` navigation -> `AuthInterceptor` rewrites to `LoginScreen(returnTo = originalScreen)`
5. Flow presentation (iOS): `LoginScreen` implements `FlowScreen` -> `BridgeNavigator.goTo()` detects `FlowScreen` -> emits `PresentFlow` -> `NavigationStateManager` shows `.fullScreenCover` with inner `NavigationStack` -> inner screens' `GoTo`/`Pop` route to the flow's path. On Android, `FlowScreen` is a regular screen — nested navigation is handled by Circuit's `CircuitContent(onNavEvent)` in the Compose UI layer.

## Future: Feature Flag Interceptor

Gate screens behind feature flags. When a feature is disabled, the interceptor can redirect to a fallback screen or silently skip navigation.

```kotlin
class FeatureFlagInterceptor(
    private val featureManager: FeatureManager,
    private val screenToFeatureMap: Map<KClass<out Screen>, String>,
    private val fallbackScreen: Screen? = null,
) : NavigationInterceptor {

    override fun interceptGoTo(screen: Screen): InterceptResult {
        val flag = screenToFeatureMap[screen::class] ?: return InterceptResult.Skip
        if (!featureManager.isEnabled(flag)) {
            return fallbackScreen?.let { InterceptResult.Rewrite(it) }
                ?: InterceptResult.Skip
        }
        return InterceptResult.Skip
    }
}
```

Wire alongside auth:

```kotlin
InterceptingNavigator(
    delegate = circuitNavigator,
    interceptors = listOf(
        FeatureFlagInterceptor(featureManager, mapOf(
            NewFeatureScreen::class to "new_feature_enabled",
        )),
        AuthInterceptor(authManager) { LoginScreen(returnTo = it) },
    ),
)
```

**Interceptor ordering matters**: feature flags should run *before* auth — no point redirecting to login for a feature that's disabled.

## Navigation Event Listeners

Navigation event listeners observe navigation actions without modifying them. Unlike `NavigationInterceptor` (which can rewrite or skip navigation), listeners are pure observers — they fire after successful navigation.

```kotlin
interface NavigationEventListener {
    fun onGoTo(screen: Screen) {}
    fun onResetRoot(screen: Screen) {}
}
```

`InterceptingNavigator` accepts an optional `listeners` list. Listeners fire:
- **After** interception (they receive the resolved/rewritten screen, not the original)
- **After** successful navigation (`goTo` only when delegate returns `true`; `resetRoot` always)

### AnalyticsNavigationListener

Tracks screen views automatically on `goTo` and `resetRoot` navigation:

```kotlin
class AnalyticsNavigationListener(
    private val analyticsDispatcher: AnalyticsDispatcher,
) : NavigationEventListener {
    override fun onGoTo(screen: Screen) {
        analyticsDispatcher.trackScreenView(screen::class.simpleName ?: "Unknown")
    }
    override fun onResetRoot(screen: Screen) {
        analyticsDispatcher.trackScreenView(screen::class.simpleName ?: "Unknown")
    }
}
```

Wired on both platforms alongside the auth interceptor:

```kotlin
InterceptingNavigator(
    delegate = circuitNavigator,
    interceptors = listOf(AuthInterceptor(...)),
    listeners = listOf(AnalyticsNavigationListener(graph.analyticsDispatcher)),
)
```

### Screen View Tracking Coverage

| Navigation path | Android | iOS |
|---|---|---|
| Cold start (HomeScreen) | Always tracks HomeScreen via `analyticsListener.onResetRoot(HomeScreen)` in `LaunchedEffect` | Always tracks HomeScreen via `analyticsListener.onResetRoot(HomeScreen)` in `init` |
| Tab switch (bottom nav) | `navigator.resetRoot(target)` → listener fires | Swift `onChange(of: selectedTab)` → `notifyTabSelected` → `onSwitchTab` callback |
| Push screen (`goTo`) | `navigator.goTo(screen)` → listener fires | Same — goes through `InterceptingNavigator` |
| Deep link | Bypasses `InterceptingNavigator`, manually tracks only the final destination via `intercepted.last()` | Same — only final destination tracked |

**Pop is not tracked.** When the user navigates back, the revealed screen does not get a new screen view event. Our custom `NavigationEventListener` doesn't observe `pop` — and doing so naively would fire intermediate screen views on multi-pop (e.g., popping from screen 4 back to screen 1 would track screens 3, 2, 1 even though the user never sees them). Circuit's `circuitx-navigation` `NavigationEventListener` solves this by passing the full backstack after pop — see the migration plan in `FUTURE_PLANS.md`.

**iOS tab switch detail**: `BridgeNavigator` has `notifyTabSelected(tag)` called from Swift's `onChange(of: selectedTab)`. A `suppressTabCallback` flag prevents double-tracking when `switchTab` is called programmatically (deep links) — `switchTab` sets the flag, `notifyTabSelected` checks and resets it.

### Deep Link Analytics

Deep links bypass `InterceptingNavigator` for the actual navigation (to avoid tracking intermediate screens) and explicitly track only the final destination:

```
URI "mockdonalds://app/more/profile"
  → parse: [MoreScreen, ProfileScreen]
  → intercept: auth-check each screen
  → navigate: push all screens onto the stack
  → track: analyticsListener.onGoTo(ProfileScreen)  ← only the last screen
```

**Android**: `circuitNavigator.resetRoot(first)` + `circuitNavigator.goTo(rest)` bypasses the listener-equipped `navigator`. Then `analyticsListener.onGoTo(intercepted.last())` fires once.

**iOS**: `bridgeNavigator.switchTab(first.tag)` + `bridgeNavigator.goTo(rest)` bypasses `interceptingNavigator`. Then `analyticsListener.onGoTo(intercepted.last())` fires once. The `switchTab` call sets `suppressTabCallback` so Swift's `onChange` doesn't double-track the tab.

### Cold Start Analytics

Both platforms track HomeScreen exactly once on cold start — it's the root screen:
- **Android**: `LaunchedEffect(Unit)` calls `analyticsListener.onResetRoot(HomeScreen)` — runs once, independent of deep links
- **iOS**: `IosApp.init` calls `analyticsListener.onResetRoot(HomeScreen)` — runs once at construction

In-app deep links (while the app is already open) do NOT re-track HomeScreen — only the final destination is tracked.
