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

## Bottom Sheets

Bottom sheets are **UI state owned by the presenting screen**, not independent Circuit screens on the backstack.

### Why Not a Circuit Screen?

A Circuit `Screen` has its own presenter, lifecycle, and backstack entry. Bottom sheets are transient UI that:
- Don't belong on the navigation backstack
- Can't use `PopResult` (not on the stack)
- Create awkward ownership splits (who calls `show()` vs `dismiss()`?)

Making a bottom sheet a full Screen forces a parallel result system and couples two unrelated presenters through a shared side channel.

### Pattern: State-Hoisted Bottom Sheet

The presenting screen's **presenter owns all sheet state**. The **UI renders the sheet** based on that state and hoists actions back via `eventSink`.

```
MorePresenter (owns loginSheet state)
  └─ MoreUi / MoreView
       └─ ModalBottomSheet / .sheet
            └─ LoginSheetContent(email, onEmailChanged, onSignIn)
```

### Implementation

**UiState** — nullable sheet state signals show/hide:

```kotlin
data class MoreUiState(
    val loginSheet: LoginSheetState? = null,  // null = hidden
    val eventSink: (MoreEvent) -> Unit,
) : CircuitUiState

data class LoginSheetState(
    val email: String = "",
)

sealed class MoreEvent {
    data class MenuItemClicked(val id: String) : MoreEvent()
    data class LoginEmailChanged(val value: String) : MoreEvent()
    data object LoginSignInConfirmed : MoreEvent()
    data object LoginSheetDismissed : MoreEvent()
}
```

**Presenter** — manages sheet lifecycle:

```kotlin
is MoreEvent.MenuItemClicked -> {
    loginSheet = LoginSheetState()
}
is MoreEvent.LoginSignInConfirmed -> {
    loginSheet = null
}
is MoreEvent.LoginSheetDismissed -> {
    loginSheet = null
}
```

**Android UI** — uses `ModalBottomSheet` with `rememberModalBottomSheetState` for animated show/hide. A private `LoginBottomSheet` composable encapsulates the sheet state, animation, and dialog:

```kotlin
@Composable
private fun LoginBottomSheet(
    loginSheet: LoginSheetState?,
    onEmailChanged: (String) -> Unit,
    onSignInConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // ...
    if (loginSheet != null) {
        ModalBottomSheet(onDismissRequest = onDismissed, sheetState = sheetState) {
            LoginSheetContent(...)
        }
    }
}
```

**iOS View** — uses `.sheet(isPresented:onDismiss:)` driven by a local `@State` synced from the presenter state via `.onChange(of:)`:

```swift
.onChange(of: state.loginSheet != nil) { isPresented in
    showLoginSheet = isPresented
}
.sheet(isPresented: $showLoginSheet, onDismiss: {
    state.eventSink(MoreEvent.LoginSheetDismissed())
}) {
    loginSheetContent
}
```

### When the Sheet Content is Complex

The sheet content can be as complex as needed — it's just a composable/View with hoisted callbacks. If the presenter gets too heavy, extract business logic into use cases. The presenter stays thin, wiring state and delegating to interactors.

## Dialogs

Dialogs are **screen-local UI state** — owned by the UI layer, not the presenter.

### Why Not the Presenter?

A dialog showing/hiding is a UI concern. The presenter only needs to know when the user *confirms* — not that a dialog was shown. This keeps dialog lifecycle out of shared code and lets each platform use its native dialog API directly.

### Pattern

1. **UI owns dialog visibility** — `remember { mutableStateOf(false) }` on Android, `@State` on iOS
2. **Button tap shows dialog** — UI-local, no event to presenter
3. **Dialog confirm sends event** — `eventSink(SignInConfirmed)` triggers presenter logic (navigation, state change)
4. **Dialog dismiss hides dialog** — UI-local, no event to presenter

**Android:**
```kotlin
var showSignInDialog by remember { mutableStateOf(false) }

if (showSignInDialog) {
    AlertDialog(
        onDismissRequest = { showSignInDialog = false },
        confirmButton = {
            TextButton(onClick = {
                showSignInDialog = false
                state.eventSink(LoginEvent.SignInConfirmed)
            }) { Text("Send Link") }
        },
        // ...
    )
}

// Button triggers dialog, not an event:
Button(onClick = { showSignInDialog = true }) { Text("Sign In") }
```

**iOS:**
```swift
@State private var showSignInDialog = false

.alert("Sign In", isPresented: $showSignInDialog) {
    Button("Send Link") {
        state.eventSink(LoginEvent.SignInConfirmed())
    }
    Button("Cancel", role: .cancel) {}
}

// Button triggers dialog:
Button(action: { showSignInDialog = true }) { Text("Sign In") }
```

### Testing

- **Presenter tests**: verify `SignInConfirmed` triggers navigation — no dialog state to test
- **UI tests**: tap button → assert dialog appears → tap confirm → assert event fired

## Nested Circuit Flows (Circuit-in-Circuit)

A Circuit screen's UI can host its own `NavigableCircuitContent` with an independent backstack. This enables multi-step flows inside a container without affecting the app's main navigation.

### Android Support

On Android, this works natively. Inside any composable (including a `ModalBottomSheet`), you can drop a `NavigableCircuitContent` with its own `rememberSaveableBackStack` and `rememberCircuitNavigator`. Inner screens navigate independently of the app's main backstack.

```kotlin
@Composable
fun LoginFlowUi(state: LoginFlowUiState, modifier: Modifier = Modifier) {
    val backStack = rememberSaveableBackStack(root = EmailEntryScreen)
    val navigator = rememberCircuitNavigator(backStack) {
        state.eventSink(LoginFlowEvent.FlowDismissed)
    }
    NavigableCircuitContent(navigator = navigator, backStack = backStack)
}
```

### iOS Limitation

Nested Circuit flows are **not currently supported on iOS** with our bridge architecture. The `BridgeNavigator` is a single shared instance — `goTo`/`pop` from inner screens would drive the app's main `NavigationStack`, not a nested one.

Supporting this would require:
- A **scoped navigator** — a second `BridgeNavigator` instance per nested flow
- The sheet's SwiftUI host embedding its own `CircuitNavigator` with that scoped navigator
- Presenters inside the flow receiving the scoped navigator instead of the root one

For now, use **state-hoisted bottom sheets** (single-screen content owned by the host presenter) on both platforms. If a multi-step flow in a sheet is required, it can be implemented on Android with nested Circuit while iOS uses a sequential state machine in the presenter — or the scoped navigator approach can be built out.

### When to Use What

| Scenario | Approach |
|----------|----------|
| Single-screen bottom sheet | State-hoisted: presenter owns sheet state, UI renders content |
| Confirmation dialog | UI-local state: `remember`/`@State` boolean, confirm fires event |
| Full-screen navigation | `navigator.goTo(screen)` — standard Circuit navigation |
| Multi-step flow in sheet (Android only) | Nested `NavigableCircuitContent` with independent backstack |

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
- `./gradlew :konsist:test` — architecture enforcement
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
