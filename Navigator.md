# Circuit Navigator Bridge — Cross-Platform Navigation

## Problem

Circuit's `Navigator` works natively on Android via `NavigableCircuitContent` + `SaveableBackStack`. On iOS, presenters received `Navigator.NoOp` — all navigation calls were silently dropped.

## Solution: BridgeNavigator

Extend the same bridge pattern used for presenter state (`CircuitPresenterKotlinBridge` + `@NativeCoroutinesState`) to navigation. A `BridgeNavigator` implements Circuit's `Navigator` interface and emits navigation actions as a `StateFlow<NavigationAction>` that SwiftUI observes.

## Architecture

```
Presenter calls navigator.goTo(LoginScreen)

  Android (native Circuit):
    Navigator → BackStack → NavigableCircuitContent renders screen

  iOS (bridge):
    BridgeNavigator → StateFlow<NavigationAction>
      → @NativeCoroutinesState generates navigationActionFlow
      → Swift CircuitNavigator observes via asyncSequence(for:)
      → Drives SwiftUI NavigationStack
```

## NavigationAction

Each action carries the navigation *intent* so SwiftUI knows how to handle it:

| Action | SwiftUI Behavior |
|--------|-----------------|
| `GoTo(screen)` | Push onto `NavigationStack` (slide from right) |
| `Pop` | Pop from `NavigationStack` |
| `ResetRoot(screen)` | Clear navigation path |
| `DeepLink(screens)` | Seed `NavigationStack` path with full stack |

## BridgeNavigator (Kotlin — iosMain)

- Implements `com.slack.circuit.runtime.Navigator`
- Single shared instance across all presenters (navigation is global)
- `@NativeCoroutinesState` on the `StateFlow` generates Swift-accessible `navigationActionFlow`
- `consume()` resets to `Idle` after Swift handles the action
- Additional method beyond Navigator: `deepLink(screens)`
- Stub methods (`forward`, `peek`, `peekBackStack`, `peekNavStack`) satisfy the interface — Swift owns the real stack

## CircuitNavigator (Swift)

The iOS equivalent of Android's `NavigableCircuitContent`. Wraps content in a `NavigationStack` and observes the bridge.

- Manages `[ScreenEntry]` path for push transitions
- `ScreenEntry` is a `Hashable` + `Identifiable` wrapper around Circuit screens (with UUID), enabling the same screen type to appear multiple times in the stack
- Observes `navigationActionFlow` via `asyncSequence(for:)` and pushes/pops the `NavigationStack`
- Each pushed screen is rendered via `CircuitContent(screen:)` — same presenter + UI factory system as tab screens

## Consume Pattern

After SwiftUI handles a navigation action, it calls `navigator.consume()` to reset the flow to `Idle`. This prevents:
- Replaying stale navigation events
- State desync between Kotlin and Swift

## Predictive Back (Android)

Android's predictive back gesture (slide-from-edge with preview animation) requires specific setup:

1. **Manifest flag**: `android:enableOnBackInvokedCallback="true"` in `AndroidManifest.xml` — opts into the new back API
2. **`circuitx-gesture-navigation`**: Provides `GestureNavigationDecorationFactory` which decorates screen transitions with predictive back animations
3. **2-param `rememberCircuitNavigator`**: Use `rememberCircuitNavigator(backStack, onRootPop)` — the 2-param overload that does **not** install its own back handler. The gesture navigation decoration factory handles back via `onBackInvoked = navigator::pop`

```kotlin
val navigator = rememberCircuitNavigator(
    backStack = backStack,
    onRootPop = { },
)
NavigableCircuitContent(
    navigator = navigator,
    backStack = backStack,
    decoratorFactory = remember(navigator) {
        GestureNavigationDecorationFactory(onBackInvoked = navigator::pop)
    },
)
```

**Why not `enableBackHandler = true`?** The 3-param overload with `enableBackHandler` installs a standard `BackHandler` that intercepts the back gesture *before* the predictive animation can run, resulting in an immediate pop with no animation.

**Why not just the manifest flag alone?** Without `GestureNavigationDecorationFactory`, the system handles back but Circuit doesn't animate the transition — the screen just snaps.

## Bottom Sheet Navigation

Cross-platform system that lets presenters present a full Circuit `Screen` as a modal bottom sheet with a single `suspend` call, receiving the result when the sheet is dismissed.

This is distinct from Circuit's own `Overlay` system (`OverlayHost`, `BottomSheetOverlay`) which renders inline composable content — not full screens with their own presenters. Circuit's overlays are for simple, self-contained UI (confirmation dialogs, pickers). `BottomSheetNavigator` is for presenting complete screens modally (e.g., showing LoginScreen as a sheet from the More tab).

### Architecture

```
Presenter calls bottomSheet.show(LoginScreen)
  ↓ suspend — awaits CompletableDeferred<BottomSheetResult>

  Android:
    BottomSheetNavigatorImpl.request → collectAsState() in App.kt
      → ModalBottomSheet { CircuitContent(screen) }
      → onDismissRequest → complete(Dismissed)
      → presenter resumes with BottomSheetResult.Dismissed

  iOS:
    BridgeNavigator.bottomSheetRequest → @NativeCoroutinesState
      → Swift asyncSequence(for: bottomSheetRequestFlow)
      → .sheet { CircuitContent(screen) }
      → onDismiss → completeBottomSheet(result: Dismissed)
      → presenter resumes with BottomSheetResult.Dismissed
```

### BottomSheetNavigator (core:circuit)

Platform-agnostic interface available to presenters via `LocalBottomSheetNavigator`:

```kotlin
interface BottomSheetNavigator {
    suspend fun show(screen: Screen): BottomSheetResult
}

sealed class BottomSheetResult {
    data object Confirmed : BottomSheetResult()
    data object Dismissed : BottomSheetResult()
}
```

Provided as a `staticCompositionLocalOf` with a no-op default (returns `Dismissed`). This follows the same pattern as Circuit's own `LocalOverlayHost` — the default enables presenter unit tests without needing to provide the local, while real bottom sheet interactions are tested at the UI/integration level.

Presenters access it in their `@Composable present()` body:

```kotlin
@CircuitInject(MoreScreen::class, AppScope::class)
@Inject
@Composable
fun MorePresenter(navigator: Navigator, ...): MoreUiState {
    val bottomSheet = LocalBottomSheetNavigator.current
    val centerPost = rememberCenterPost(dispatchers)

    return MoreUiState(
        eventSink = { event ->
            when (event) {
                is MoreEvent.MenuItemClicked -> centerPost {
                    val result = bottomSheet.show(LoginScreen)
                    // resumes here when sheet is dismissed
                }
            }
        },
    )
}
```

### BottomSheetNavigatorImpl (core:circuit)

Shared implementation backed by `MutableStateFlow<BottomSheetRequest?>` + `CompletableDeferred<BottomSheetResult>`:

- `show(screen)` — sets the request and suspends on a deferred
- `complete(result)` — called by the host when the sheet is dismissed, completes the deferred and clears the request
- `request: StateFlow` — observed by the platform host to render the sheet

### Android Integration

`App.kt` creates a `BottomSheetNavigatorImpl` and provides it via `LocalBottomSheetNavigator`. It collects the `request` flow and renders `ModalBottomSheet` with `CircuitContent(screen)` for the presented screen.

### iOS Integration

`BridgeNavigator` implements `BottomSheetNavigator` by delegating to `BottomSheetNavigatorImpl`. It exposes `bottomSheetRequest` as a `@NativeCoroutinesState StateFlow` which generates `bottomSheetRequestFlow` for Swift.

`CircuitNavigator.swift` observes `bottomSheetRequestFlow` via a second `.task {}` and presents sheets using `.sheet()`. On dismiss, it calls `navigator.completeBottomSheet(result:)` which completes the Kotlin deferred.

`CircuitPresenterKotlinBridge` provides `LocalBottomSheetNavigator` in the Molecule composition so presenters can access it.

### Testing

Bottom sheet rendering is a UI/integration concern — presenter unit tests verify state logic using the no-op default CompositionLocal. Actual sheet presentation and dismissal behavior is tested at the UI test level where the full composition (host + `ModalBottomSheet`/`.sheet`) is exercised.

### Key Files

| File | Role |
|------|------|
| `core/circuit/src/commonMain/.../bottomsheet/BottomSheetNavigator.kt` | Interface, result types, CompositionLocal, shared impl |
| `composeApp/src/iosMain/.../bridge/BridgeNavigator.kt` | Implements BottomSheetNavigator, exposes flow for Swift |
| `composeApp/src/iosMain/.../bridge/CircuitPresenterKotlinBridge.kt` | Provides LocalBottomSheetNavigator to presenters |
| `composeApp/src/androidMain/.../App.kt` | Bottom sheet host — renders ModalBottomSheet |
| `iosApp/iosApp/Circuit/CircuitNavigator.swift` | Observes bottomSheetRequestFlow, shows .sheet |

## Future: Auth Interceptor

`circuitx-navigation` provides `InterceptingNavigator` + `NavigationInterceptor`:

```kotlin
class AuthInterceptor(private val authManager: AuthManager) : NavigationInterceptor {
    override fun goTo(screen: Screen): InterceptedGoToResult {
        if (screen is ProtectedScreen && !authManager.isLoggedIn()) {
            return InterceptedGoToResult.Rewrite(LoginScreen(afterLoginDestination = screen))
        }
        return NavigationInterceptor.Skipped
    }
}
```

Interceptors wrap the underlying navigator — shared auth logic in `commonMain`.

## Future: Deep Links

Parse URI → `List<Screen>` in `commonMain`. Entry points:
- Android: `Activity.intent` → seed back stack
- iOS: `SceneDelegate.onOpenURL` → `bridge.deepLink(screens)` → `CircuitNavigator` seeds `NavigationStack` path

## Module Structure

Each feature follows this module layout:

```
features/<feature>/
  api/
    domain/     → Domain contracts: use case abstractions, models (no Circuit dependency)
    navigation/ → Screen object (@Parcelize), TestTags object (Circuit dependency)
  domain/       → Use case implementations (depends on api:domain only)
  data/         → Repository implementations
  presentation/ → Presenter + UI (depends on both api:domain + api:navigation)
  test/         → Fakes/fixtures (depends on api:domain only)
```

This ensures the domain layer has zero UI framework awareness — Circuit stays in `api:navigation` and `presentation`.

## Adding a New Feature/Screen

### 1. Create `api:domain` module

```
features/<feature>/api/domain/build.gradle.kts  → mockdonalds.kmp.library plugin
  dependencies: api(:core:common), api(:core:centerpost), api(kotlinx.coroutines.core)
```

Add domain contracts:
- `Get<Feature>Content.kt` — abstract use case extending `CenterPostSubjectInteractor`
- `<Feature>Models.kt` — data classes for domain models (immutable, val only)

### 2. Create `api:navigation` module

```
features/<feature>/api/navigation/build.gradle.kts  → mockdonalds.kmp.library plugin
  dependencies: api(circuit.runtime), api(:core:common)
```

Add:
- `<Feature>Screen.kt` — `@Parcelize object <Feature>Screen : Screen` in the `navigation` package
- `<Feature>TestTags.kt` — `object <Feature>TestTags` with const val tags in the `ui` package

### 3. Create `domain` module

- `Get<Feature>ContentImpl.kt` with `@ContributesBinding(AppScope::class)`
- `<Feature>Repository.kt` interface
- `Get<Feature>ContentImplTest.kt` in commonTest

### 4. Create `data` module

- `<Feature>RepositoryImpl.kt` with `@ContributesBinding(AppScope::class)`
- `<Feature>RepositoryImplTest.kt` in commonTest

### 5. Create `presentation` module

- `<Feature>Presenter.kt` — `@CircuitInject @Inject @Composable` function
- `<Feature>UiState.kt` — data class implementing `CircuitUiState` with `eventSink`
- `<Feature>Ui.kt` (androidMain) — Compose UI with `@CircuitInject`
- `<Feature>PresenterTest.kt` in commonTest
- Android UI tests: `<Feature>UiTest.kt`, `<Feature>UiRobot.kt`, `<Feature>StateRobot.kt` in androidDeviceTest

### 6. Create `test` module

- `Fake<UseCase>.kt` — fake implementation of each abstract use case

### 7. Create iOS view

- `iosApp/iosApp/Features/<Feature>/<Feature>View.swift` — SwiftUI view consuming UiState
- Add to Xcode project (project.pbxproj)
- `CircuitContent` screen factory switch case

### 8. Wire into the app

- `settings.gradle.kts` — **automatic**: feature modules are auto-discovered from `features/` directories with architecture-enforced submodules (api:domain, api:navigation, data, domain, presentation, test)
- `composeApp/build.gradle.kts` — **automatic**: feature deps are auto-discovered with enforced wiring (api:domain + api:navigation as `api()`, data + domain as `implementation()`, presentation as `api()`)
- Cross-feature navigation: depend on `<other>:api:navigation` (not full api) in the feature's presentation build.gradle.kts

### 9. Verify

- `./gradlew assemble` — full build
- `./gradlew testAndroidHostTest` — unit tests
- `./gradlew :konsist:test` — architecture enforcement (96 tests)
- `./gradlew detektMetadataCommonMain` — lint
- `swiftlint lint iosApp` — Swift lint
- `swift test --package-path iosApp/ArchitectureCheck` — Harmonize

## Key Files

| File | Role |
|------|------|
| `composeApp/src/iosMain/.../bridge/NavigationAction.kt` | Sealed class — navigation intent types |
| `composeApp/src/iosMain/.../bridge/BridgeNavigator.kt` | Navigator impl → StateFlow for iOS |
| `composeApp/src/iosMain/.../bridge/IosApp.kt` | Creates BridgeNavigator, passes to presenters |
| `composeApp/src/androidMain/.../App.kt` | Android: NavigableCircuitContent + backstack + predictive back |
| `iosApp/iosApp/Circuit/CircuitNavigator.swift` | SwiftUI navigation host — observes bridge |
| `iosApp/iosApp/Circuit/Circuit.swift` | Exposes navigator to SwiftUI |
| `iosApp/iosApp/MockDonaldsApp.swift` | Wraps TabView with CircuitNavigator |
