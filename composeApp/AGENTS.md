# composeApp Module

## Purpose

Application shell that wires all feature modules together. Contains platform entry points, navigation infrastructure, and the Metro DI graph. No business logic belongs here.

## Key Files

### commonMain

| File | Purpose |
|------|---------|
| `AppGraph.kt` | Metro `@DependencyGraph(AppScope)` root. Provides `Circuit` (assembled from multibindings of Presenter.Factory and Ui.Factory) and `AuthManager`. `CircuitProviders` interface contributes the Circuit builder. |
| `navigation/DeepLinkParser.kt` | Parses URI paths into Screen lists. `findTabByTag()` resolves tab screens. `createDeepLinkParser()` registers all tab screens plus profile/login routes. |
| `navigation/NavigationInterceptor.kt` | `NavigationInterceptor` interface with `interceptGoTo`/`interceptResetRoot` returning `InterceptResult` (Skip or Rewrite). |
| `navigation/AuthInterceptor.kt` | Redirects `ProtectedScreen` navigation to `LoginScreen` when user is not authenticated. |
| `navigation/InterceptingNavigator.kt` | Wraps Circuit's `Navigator`, applies interceptor chain to `goTo`/`resetRoot`. Also provides `deepLink()` to intercept a list of screens. |

### androidMain

| File | Purpose |
|------|---------|
| `App.kt` | `MockDonaldsApp` composable -- creates `AppGraph`, sets up `rememberSaveableBackStack(root = HomeScreen)`, wires `InterceptingNavigator` with `AuthInterceptor`, handles deep link intents, renders `NavigableCircuitContent` with gesture navigation and `MockDonaldsBottomNavigation`. |
| `MockDonaldsBottomNavigation.kt` | Custom bottom nav bar with glass effect. Uses `TabScreen.tag` for route matching. |
| `MockDonaldsIcons.kt` | Custom `ImageVector` icons for bottom navigation tabs. |

### iosMain (KMP-to-Swift Bridge)

| File | Purpose |
|------|---------|
| `bridge/IosApp.kt` | iOS entry point. Creates `AppGraph`, `BridgeNavigator`, `InterceptingNavigator` with `AuthInterceptor`. Exposes `presenterBridge(screen)` for Swift and `deepLink(uri)` with tab-aware routing. |
| `bridge/BridgeNavigator.kt` | iOS `Navigator` implementation using `Channel<List<NavigationAction>>`. Batches synchronous navigator calls via `dispatch_async(dispatch_get_main_queue())` to avoid SwiftUI animation artifacts. |
| `bridge/NavigationAction.kt` | Sealed class: GoTo, Pop, ResetRoot, SwitchTab, DeepLink. Actions are batched into lists for single-frame SwiftUI updates. |
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

## Future: Analytics Navigation Event Listener

Track screen views and navigation events for analytics and crash breadcrumbs. This is an **observer**, not a rewriter — it never modifies the navigation action.

```kotlin
class AnalyticsNavigationListener(
    private val analytics: AnalyticsTracker,
) : NavigationInterceptor {

    override fun interceptGoTo(screen: Screen): InterceptResult {
        analytics.trackScreenView(screen::class.simpleName ?: "Unknown")
        return InterceptResult.Skip  // always pass through
    }

    override fun interceptResetRoot(screen: Screen): InterceptResult {
        analytics.trackEvent("deep_link_landed", mapOf("screen" to screen::class.simpleName))
        return InterceptResult.Skip
    }
}
```

**Ordering**: analytics should run *last* so it logs the final (potentially rewritten) screen, not the original intent.
