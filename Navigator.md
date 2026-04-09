# Circuit Navigator Bridge — Cross-Platform Navigation

## Problem

Circuit's `Navigator` works natively on Android via `NavigableCircuitContent` + `SaveableBackStack`. On iOS, presenters received `Navigator.NoOp` — all navigation calls were silently dropped.

## Solution: BridgeNavigator

Extend the same bridge pattern used for presenter state (`CircuitPresenterKotlinBridge` + `@NativeCoroutinesState`) to navigation. A `BridgeNavigator` implements Circuit's `Navigator` interface and emits batched navigation actions through a `Channel` that SwiftUI observes via `receiveAsFlow()`.

## Architecture

```
Presenter calls navigator.goTo(LoginScreen)

  Android (native Circuit):
    Navigator → BackStack → NavigableCircuitContent renders screen
    (multiple calls in one event handler → one Compose frame → one render)

  iOS (bridge):
    BridgeNavigator enqueues action → pending list accumulates
      → dispatch_async schedules flush for next main run loop tick
      → flush sends List<NavigationAction> through Channel
      → receiveAsFlow() + @NativeCoroutines exposes to Swift
      → CircuitNavigator processes full batch in one SwiftUI update
```

## NavigationAction

Each action carries the navigation *intent* so SwiftUI knows how to handle it:

| Action | SwiftUI Behavior |
|--------|-----------------|
| `GoTo(screen)` | Push onto `NavigationStack` (slide from right) |
| `Pop` | Pop from `NavigationStack` |
| `ResetRoot(screen)` | Clear navigation path |
| `SwitchTab(tag)` | Clear navigation path and switch to tab with given tag |
| `DeepLink(screens)` | Seed `NavigationStack` path with full stack |

Actions are batched into lists before delivery. Multiple actions from a single synchronous event handler (e.g. `pop()` + `goTo()` in the login return flow) arrive as one batch, processed in one SwiftUI update cycle.

## TabScreen Interface (commonMain — core:circuit)

Tab screens implement `TabScreen` from `core:circuit`, which extends `Screen` with a `tag` property:

```kotlin
interface TabScreen : Screen {
    val tag: String
}
```

Each bottom-nav screen defines its own tag:

```kotlin
@Parcelize
data object HomeScreen : TabScreen {
    override val tag: String = "home"
}
```

The tag is the **single source of truth** for tab identification across all platforms:

- **Android** — `(topScreen as? TabScreen)?.tag` determines bottom nav state; `tabScreens.firstOrNull { it.tag == route }` handles tab switching
- **iOS Kotlin** — `IosApp.deepLink()` uses `screen is TabScreen` + `screen.tag` to emit `SwitchTab` instead of pushing tab screens onto the navigation stack
- **Swift** — `.tag(HomeScreen.shared.tag)` on TabView items reads from the Kotlin-defined property, no hardcoded strings

Enforced by Konsist: all `TabScreen` implementations must have a `tag` property and reside in `api/navigation` packages.

## BridgeNavigator (Kotlin — iosMain)

### Why Channel?

Navigation actions are **events** (fire-and-forget commands), not **state** (latest-value-wins). The delivery primitive must match:

| Primitive | Problem for navigation |
|-----------|----------------------|
| **StateFlow** | Conflates: rapid `pop()` + `goTo()` drops the pop (latest-value-wins). Replays: last value replayed to reconnecting subscribers causes duplicate navigation. |
| **SharedFlow(replay=0)** | No conflation or replay, but intermediate emissions can buffer in the Kotlin→Swift async bridge (`asyncSequence` uses `AsyncStream`). Already-processed actions get redelivered from the buffer. |
| **Channel** | Exactly-once ordered delivery. No replay. No conflation. Values are consumed on receive — gone forever. Buffers for cold-start deep links (sits in channel until Swift starts consuming). |

### Run-loop batching

On Android, multiple synchronous navigator calls in one event handler naturally batch — they modify the in-memory `SaveableBackStack`, and Compose renders once per frame. No intermediate visual state.

On iOS without batching, each navigator call would be a separate Channel emission. Swift would process `Pop` and `GoTo` in separate SwiftUI update cycles, causing a visible "pop flash" before the push animation — a jarring artifact.

BridgeNavigator recreates Android's single-frame behavior using the main run loop as the batch boundary:

```
Event handler starts (user tapped "Sign In")
│
├─ pop() called
│    pending = [Pop]
│    flushScheduled == false → schedule flush via dispatch_async
│
├─ goTo(ProfileScreen) called
│    pending = [Pop, GoTo(Profile)]
│    flushScheduled == true → already scheduled, just append
│
Event handler returns. Current run loop tick ends.
─────────────────────────────────────────────────
Next run loop tick:
│
├─ flush() fires:
│    batch = [Pop, GoTo(Profile)]
│    pending.clear()
│    channel.trySend(batch)
│
─────────────────────────────────────────────────
Swift receives [Pop, GoTo(Profile)]
│
├─ Processes Pop: removes last from navigationPath
├─ Processes GoTo: appends Profile to navigationPath
├─ Returns → SwiftUI renders ONCE with both changes applied
```

`dispatch_async(dispatch_get_main_queue())` schedules the flush for the **next** main run loop iteration — after all synchronous code in the current tick completes. This is the iOS equivalent of Compose deferring rendering to the end of the frame.

This works for any combination of actions — `pop() + goTo()`, `resetRoot() + goTo() + goTo()`, or whatever an interceptor produces. The batching is transparent to callers.

### Buffer capacity

The Channel has a capacity of 5. Each run loop tick produces at most one batch. Swift consumes batches as fast as the run loop delivers them. 5 provides headroom for burst scenarios (rapid user interactions across consecutive ticks) while failing fast (`trySend` check) if the consumer genuinely falls behind.

### Implementation

```kotlin
class BridgeNavigator : Navigator {
    // Channel: exactly-once, ordered, no replay, no conflation
    private val _navigationActions = Channel<List<NavigationAction>>(capacity = 5)

    @NativeCoroutines
    val navigationActions: Flow<List<NavigationAction>> = _navigationActions.receiveAsFlow()

    // Accumulator for the current run loop tick's batch
    private val pending = mutableListOf<NavigationAction>()
    private var flushScheduled = false

    private fun enqueue(action: NavigationAction) {
        pending.add(action)
        if (!flushScheduled) {
            flushScheduled = true
            dispatch_async(dispatch_get_main_queue()) {
                val batch = pending.toList()
                pending.clear()
                flushScheduled = false
                check(_navigationActions.trySend(batch).isSuccess) {
                    "Navigation consumer is not keeping up"
                }
            }
        }
    }

    override fun goTo(screen: Screen): Boolean {
        enqueue(NavigationAction.GoTo(screen))
        return true
    }

    override fun pop(result: PopResult?): Screen? {
        enqueue(NavigationAction.Pop)
        return null
    }

    // resetRoot, backward, deepLink — all delegate to enqueue
}
```

- Single shared instance across all presenters (navigation is global)
- `@NativeCoroutines` on the `Flow` generates Swift-accessible `navigationActions`
- Additional method beyond Navigator: `deepLink(screens)` for deep link stack seeding
- Stub methods (`forward`, `peek`, `peekBackStack`, `peekNavStack`) satisfy the interface — Swift owns the real stack
- No `consume()` method — Channel values are consumed on receive

## CircuitNavigator (Swift)

The iOS equivalent of Android's `NavigableCircuitContent`. Wraps content in a `NavigationStack` and observes the bridge.

- Manages `[ScreenEntry]` path for push transitions
- `ScreenEntry` is a `Hashable` + `Identifiable` wrapper around Circuit screens (with UUID), enabling the same screen type to appear multiple times in the stack
- Observes `navigationActions` via `asyncSequence(for:)` — each emission is a batch of actions
- Processes the full batch in one `handleActions` call before returning to the async loop — all `navigationPath` mutations happen in one SwiftUI update cycle
- Each pushed screen is rendered via `CircuitContent(screen:)` — same presenter + UI factory system as tab screens
- `.id(entry.id)` on each destination view forces SwiftUI to create a fresh view when a screen at the same path index is replaced (e.g. batch `[Pop, GoTo]`). Without this, SwiftUI reuses the existing view and preserves its `@State`, causing a type mismatch crash when the old state (e.g. `LoginUiState`) is force-cast to the new screen's state type (e.g. `ProfileUiState`)
- No `consume()` call needed — values are removed from the Channel on receive

### Why no consume?

The previous StateFlow-based design required `consume()` to reset the flow to `Idle` after handling, preventing replay on resubscription. With Channel:
- Values are consumed on receive — once Swift gets a batch, it's gone from the Channel
- No replay — new/reconnecting subscribers start fresh
- No `Idle` sentinel needed — an empty Channel means no pending navigation

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
Presenter (owns sheet state)
  └─ Ui / View
       └─ ModalBottomSheet / .sheet
            └─ SheetContent(data, onAction, onDismiss)
```

### Implementation

**UiState** — nullable sheet state signals show/hide:

```kotlin
data class ExampleUiState(
    val detailSheet: DetailSheetState? = null,  // null = hidden
    val eventSink: (ExampleEvent) -> Unit,
) : CircuitUiState

data class DetailSheetState(
    val title: String = "",
    val description: String = "",
)

sealed class ExampleEvent {
    data class ItemClicked(val id: String) : ExampleEvent()
    data object SheetConfirmed : ExampleEvent()
    data object SheetDismissed : ExampleEvent()
}
```

**Presenter** — manages sheet lifecycle:

```kotlin
is ExampleEvent.ItemClicked -> {
    detailSheet = DetailSheetState(title = "Details", description = "...")
}
is ExampleEvent.SheetConfirmed -> {
    detailSheet = null
}
is ExampleEvent.SheetDismissed -> {
    detailSheet = null
}
```

**Android UI** — uses `ModalBottomSheet` with `rememberModalBottomSheetState` for animated show/hide:

```kotlin
@Composable
private fun DetailBottomSheet(
    detailSheet: DetailSheetState?,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // ...
    if (detailSheet != null) {
        ModalBottomSheet(onDismissRequest = onDismissed, sheetState = sheetState) {
            DetailSheetContent(detailSheet, onConfirmed)
        }
    }
}
```

**iOS View** — uses `.sheet(isPresented:onDismiss:)` driven by a local `@State` synced from the presenter state via `.onChange(of:)`:

```swift
.onChange(of: state.detailSheet != nil) { isPresented in
    showDetailSheet = isPresented
}
.sheet(isPresented: $showDetailSheet, onDismiss: {
    state.eventSink(ExampleEvent.SheetDismissed())
}) {
    DetailSheetContentView(state: state.detailSheet!)
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

## Navigation Interceptors

Navigation interceptors provide a reusable, composable way to modify navigation actions before they reach the underlying navigator. All interception logic lives in `commonMain` — shared across Android and iOS with zero platform-specific code.

### Why Custom Instead of `circuitx-navigation`?

Circuit provides `circuitx-navigation` with `InterceptingNavigator` and `NavigationInterceptor`, but that implementation is Compose-based (it operates within `@Composable` context). Our iOS bridge architecture passes a `Navigator` to presenters directly — there's no Compose composition on the iOS path. A custom interceptor that wraps any `Navigator` works identically on both platforms.

### Architecture

```
Presenter calls navigator.goTo(ProfileScreen)

  InterceptingNavigator receives the call
    ↓
  Runs interceptor chain: [AuthInterceptor, ...]
    ↓
  AuthInterceptor checks: ProfileScreen is ProtectedScreen + not authenticated
    → Rewrite to LoginScreen(returnTo = ProfileScreen)
    ↓
  Delegate navigator receives goTo(LoginScreen(returnTo = ProfileScreen))

  Android: Circuit navigator → BackStack → NavigableCircuitContent
  iOS:     BridgeNavigator → StateFlow<NavigationAction> → SwiftUI
```

### InterceptResult

Each interceptor returns one of:

| Result | Behavior |
|--------|----------|
| `Skip` | This interceptor has nothing to say — pass to the next interceptor |
| `Rewrite(screen)` | Replace the target screen — subsequent interceptors see the rewritten screen |

```kotlin
sealed class InterceptResult {
    data object Skip : InterceptResult()
    data class Rewrite(val screen: Screen) : InterceptResult()
}
```

### NavigationInterceptor Interface

```kotlin
interface NavigationInterceptor {
    fun interceptGoTo(screen: Screen): InterceptResult = InterceptResult.Skip
    fun interceptResetRoot(screen: Screen): InterceptResult = InterceptResult.Skip
}
```

Default implementations return `Skip` — interceptors only override the methods they care about. This keeps interceptors focused: an auth interceptor only needs `interceptGoTo`, while a hypothetical onboarding interceptor might only care about `interceptResetRoot`.

### InterceptingNavigator

Wraps any `Navigator` and runs the interceptor chain on `goTo` and `resetRoot`. Everything else delegates unchanged.

```kotlin
class InterceptingNavigator(
    private val delegate: Navigator,
    private val interceptors: List<NavigationInterceptor>,
) : Navigator by delegate {

    override fun goTo(screen: Screen): Boolean {
        val resolved = applyInterceptors(screen) { interceptGoTo(it) }
        return delegate.goTo(resolved)
    }

    override fun resetRoot(
        newRoot: Screen,
        options: Navigator.StateOptions,
    ): List<Screen> {
        val resolved = applyInterceptors(newRoot) { interceptResetRoot(it) }
        return delegate.resetRoot(resolved, options)
    }

    fun deepLink(screens: List<Screen>): List<Screen> {
        return screens.map { screen ->
            applyInterceptors(screen) { interceptGoTo(it) }
        }
    }

    private inline fun applyInterceptors(
        screen: Screen,
        intercept: NavigationInterceptor.(Screen) -> InterceptResult,
    ): Screen {
        var current = screen
        for (interceptor in interceptors) {
            when (val result = interceptor.intercept(current)) {
                is InterceptResult.Skip -> continue
                is InterceptResult.Rewrite -> current = result.screen
            }
        }
        return current
    }
}
```

Key design points:
- **`Navigator by delegate`** — all methods (pop, peek, peekBackStack, etc.) delegate automatically. Only `goTo` and `resetRoot` are overridden.
- **Chain composition** — interceptors run in order. A `Rewrite` from interceptor A feeds into interceptor B. This enables stacking (e.g., auth then feature flags).
- **`deepLink(screens)`** — maps each screen through `interceptGoTo`, returning the rewritten stack. This is called by platform entry points after parsing the URI.

### Platform Wiring

**Android** — wraps `rememberCircuitNavigator`:

```kotlin
val circuitNavigator = rememberCircuitNavigator(backStack, onRootPop = { })
val navigator = remember(circuitNavigator) {
    InterceptingNavigator(
        delegate = circuitNavigator,
        interceptors = listOf(
            AuthInterceptor(graph.authManager) { LoginScreen(returnTo = it) },
        ),
    )
}
NavigableCircuitContent(navigator = navigator, backStack = backStack, ...)
```

**iOS** — wraps `BridgeNavigator` in Kotlin (`iosMain`). Swift is completely unchanged:

```kotlin
// IosApp.kt (iosMain)
private val bridgeNavigator = BridgeNavigator()
private val interceptingNavigator = InterceptingNavigator(
    delegate = bridgeNavigator,
    interceptors = listOf(
        AuthInterceptor(graph.authManager) { LoginScreen(returnTo = it) },
    ),
)

// Swift observes bridgeNavigator — unchanged
val navigator: BridgeNavigator get() = bridgeNavigator

// Presenters receive interceptingNavigator — rewrites happen before StateFlow emit
fun presenterBridge(): Map<...> = mapOf(
    ...(interceptingNavigator, ...)
)
```

This is the critical insight: interception happens **in Kotlin before the navigation action is emitted to the StateFlow**. Swift's `CircuitNavigator.swift` sees the already-rewritten screen. Zero changes to Swift navigation code.

### File Locations

| File | Purpose |
|------|---------|
| `composeApp/src/commonMain/.../navigation/NavigationInterceptor.kt` | `InterceptResult` + `NavigationInterceptor` interface |
| `composeApp/src/commonMain/.../navigation/InterceptingNavigator.kt` | `InterceptingNavigator` wrapper |
| `composeApp/src/commonMain/.../navigation/AuthInterceptor.kt` | Auth-gating interceptor |
| `composeApp/src/commonMain/.../navigation/DeepLinkParser.kt` | URI → screen list parser |

---

## Auth Interceptor

### Problem

Protected screens (e.g., Profile) should require authentication. Without interception, every presenter that navigates to a protected screen would need to manually check auth state and redirect — duplicating logic across features and coupling them to login.

### Solution

A `ProtectedScreen` marker interface and an `AuthInterceptor` that automatically redirects unauthenticated users to login, with a `returnTo` parameter so login can redirect back after authentication.

### ProtectedScreen Marker

```kotlin
// core/circuit/src/commonMain/.../core/circuit/ProtectedScreen.kt
interface ProtectedScreen : Screen
```

Lives in `core:circuit` because feature `api:navigation` modules need to implement it. Any screen that extends `ProtectedScreen` instead of `Screen` is automatically auth-gated by the interceptor.

```kotlin
// features/profile/api/navigation/.../ProfileScreen.kt
@Parcelize
data object ProfileScreen : ProtectedScreen
```

### AuthInterceptor

```kotlin
class AuthInterceptor(
    private val authManager: AuthManager,
    private val loginScreenFactory: (returnTo: Screen) -> Screen,
) : NavigationInterceptor {

    override fun interceptGoTo(screen: Screen): InterceptResult {
        if (screen is ProtectedScreen && !authManager.isAuthenticated) {
            return InterceptResult.Rewrite(loginScreenFactory(screen))
        }
        return InterceptResult.Skip
    }
}
```

The `loginScreenFactory` lambda decouples the interceptor from `LoginScreen` — the wiring site provides `{ LoginScreen(returnTo = it) }`.

### core:auth Module

Auth state is a cross-cutting concern — multiple features and the interceptor need to check/modify it. The interface lives in `core:auth:api` and the implementation in `core:auth:impl`.

```kotlin
// core/auth/api/.../AuthManager.kt
interface AuthManager {
    val isAuthenticated: Boolean
    fun login()
    fun logout()
}

// core/auth/impl/.../InMemoryAuthManager.kt
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class InMemoryAuthManager @Inject constructor() : AuthManager {
    override var isAuthenticated: Boolean = false
        private set
    override fun login() { isAuthenticated = true }
    override fun logout() { isAuthenticated = false }
}
```

`@SingleIn(AppScope)` ensures a single instance across the entire app — the interceptor and presenters see the same auth state.

### Return-After-Auth Flow

The complete flow when an unauthenticated user tries to access ProfileScreen:

```
1. MorePresenter: navigator.goTo(ProfileScreen)
2. InterceptingNavigator: ProfileScreen is ProtectedScreen + not authenticated
   → Rewrite to LoginScreen(returnTo = ProfileScreen)
3. delegate.goTo(LoginScreen(returnTo = ProfileScreen))
4. LoginScreen renders, user signs in
5. LoginPresenter:
   - authManager.login()        // sets isAuthenticated = true
   - navigator.pop()            // dismiss LoginScreen
   - navigator.goTo(returnTo)   // navigate to ProfileScreen
6. InterceptingNavigator: ProfileScreen is ProtectedScreen + now authenticated
   → Skip (pass through)
7. delegate.goTo(ProfileScreen)  // user arrives at ProfileScreen
```

**LoginScreen parameterization:**

```kotlin
@Parcelize
data class LoginScreen(val returnTo: Screen? = null) : Screen
```

Changed from `data object` to `data class` to carry the return destination. Circuit's codegen injects the `screen` parameter into the presenter:

```kotlin
@CircuitInject(LoginScreen::class, AppScope::class)
@Inject
@Composable
fun LoginPresenter(
    screen: LoginScreen,      // Circuit injects the screen instance
    navigator: Navigator,
    authManager: AuthManager,
    // ...
): LoginUiState {
    // On sign-in confirm:
    authManager.login()
    navigator.pop()
    screen.returnTo?.let { navigator.goTo(it) }
}
```

This is analogous to Compose Navigation's serialized destination arguments — the `Screen` is a data class that carries typed parameters, and Circuit deserializes/injects it into the presenter.

### Already-Authenticated Flow

When the user is already authenticated:

```
1. MorePresenter: navigator.goTo(ProfileScreen)
2. InterceptingNavigator: ProfileScreen is ProtectedScreen + authenticated
   → Skip
3. delegate.goTo(ProfileScreen)  // direct navigation, no redirect
```

---

## Deep Linking

### URI Scheme

```
mockdonalds://app/<segment1>/<segment2>/...
```

Each path segment maps to a screen via a registry. Multiple segments build a back stack.

### DeepLinkParser

```kotlin
class DeepLinkParser(
    private val screenRegistry: Map<String, () -> Screen>,
) {
    fun parse(uri: String): List<Screen>? {
        val path = uri.substringAfter("://", "")
            .substringAfter("/", "")
            .trim('/')
        if (path.isEmpty()) return null

        val screens = path.split("/").mapNotNull { segment ->
            screenRegistry[segment]?.invoke()
        }
        return screens.ifEmpty { null }
    }
}
```

- Unknown segments are silently skipped (graceful degradation)
- Returns `null` for empty paths or no matches
- Screen factories are lambdas (`() -> Screen`) to support both `data object` and `data class` screens

### Screen Registry

```kotlin
val deepLinkParser = DeepLinkParser(
    screenRegistry = mapOf(
        "home" to { HomeScreen },
        "order" to { OrderScreen },
        "scan" to { ScanScreen },
        "rewards" to { RewardsScreen },
        "more" to { MoreScreen },
        "profile" to { ProfileScreen },
    ),
)
```

### Deep Link + Auth Interception

Deep links pass through the interceptor chain just like manual navigation:

Deep links must always start with a tab root screen (home, order, rewards, scan, more) — non-tab screens like profile can only appear as subsequent segments. This ensures there is always a tab bar and a sensible back destination.

```
mockdonalds://app/more/profile (unauthenticated):
  1. DeepLinkParser.parse() → [MoreScreen, ProfileScreen]
  2. InterceptingNavigator.deepLink([MoreScreen, ProfileScreen])
     → MoreScreen: not ProtectedScreen → Skip
     → ProfileScreen: ProtectedScreen + not authenticated → Rewrite
     → [MoreScreen, LoginScreen(returnTo = ProfileScreen)]
  3. resetRoot(MoreScreen), goTo(LoginScreen(returnTo = ProfileScreen))
  4. User signs in → pop LoginScreen, goTo(ProfileScreen) → lands on ProfileScreen
  5. Back → MoreScreen (tab bar visible)

mockdonalds://app/more/profile (authenticated):
  1. DeepLinkParser.parse() → [MoreScreen, ProfileScreen]
  2. InterceptingNavigator.deepLink([MoreScreen, ProfileScreen])
     → MoreScreen: Skip, ProfileScreen: authenticated → Skip
     → [MoreScreen, ProfileScreen]
  3. resetRoot(MoreScreen), goTo(ProfileScreen) → lands on ProfileScreen directly
```

### Platform Entry Points

**Android** — Intent filter in `AndroidManifest.xml`:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="mockdonalds" android:host="app" />
</intent-filter>
```

`MainActivity` passes the intent to `MockDonaldsApp`:

```kotlin
MockDonaldsApp(deepLinkIntent = intent)
```

`App.kt` handles it in a `LaunchedEffect`:

```kotlin
LaunchedEffect(deepLinkIntent) {
    val uri = deepLinkIntent?.data?.toString() ?: return@LaunchedEffect
    val screens = deepLinkParser.parse(uri) ?: return@LaunchedEffect
    val intercepted = navigator.deepLink(screens)
    if (intercepted.isNotEmpty()) {
        navigator.resetRoot(intercepted.first())
        intercepted.drop(1).forEach { navigator.goTo(it) }
    }
}
```

**iOS** — `.onOpenURL` in `MockDonaldsApp.swift`:

```swift
.onOpenURL { url in
    delegate.handleDeepLink(url: url)
}
```

`AppDelegate.swift` calls into Kotlin:

```swift
func handleDeepLink(url: URL) {
    circuit.iosApp.deepLink(uri: url.absoluteString)
}
```

`IosApp.kt` (iosMain) handles parsing, interception, and tab/push splitting:

```kotlin
fun deepLink(uri: String) {
    val screens = deepLinkParser.parse(uri) ?: return
    val intercepted = interceptingNavigator.deepLink(screens)
    if (intercepted.isEmpty()) return

    val first = intercepted.first()
    if (first is TabScreen) {
        // First screen is a tab — switch to it, push the rest
        bridgeNavigator.switchTab(first.tag)
        intercepted.drop(1).forEach { bridgeNavigator.goTo(it) }
    } else {
        // No tab root — push all screens onto current tab
        intercepted.forEach { bridgeNavigator.goTo(it) }
    }
}
```

On iOS, the TabView is the permanent NavigationStack root — tab screens must **switch the tab**, not get pushed onto the navigation path. `SwitchTab` + `GoTo` calls are batched via `dispatch_async` into one `[SwitchTab("home"), GoTo(ProfileScreen)]` delivery, processed atomically in one SwiftUI update cycle.

### Back Stack Behavior

Deep links **must always start with a tab screen** (home, order, scan, rewards, more) as the first segment. Subsequent segments push detail screens on top.

**Android** — first screen becomes `resetRoot`, rest are `goTo`:

```
mockdonalds://app/home/profile
  → resetRoot(HomeScreen)
  → goTo(ProfileScreen)
  → Back button → HomeScreen

mockdonalds://app/order
  → resetRoot(OrderScreen)
  → User sees Order tab
```

**iOS** — first screen is `SwitchTab`, rest are `goTo` (TabView is the permanent root):

```
mockdonalds://app/home/profile
  → SwitchTab("home") — switches to Home tab, clears nav path
  → GoTo(ProfileScreen) — pushes onto NavigationStack
  → Back button → Home tab

mockdonalds://app/order
  → SwitchTab("order") — switches to Order tab
  → User sees Order tab
```

There is no automatic root injection — the URI is the complete navigation stack. This keeps behavior predictable and the docs simple: what you see in the URI is what you get.

### Testing Deep Links

**Android — via adb:**
```bash
# Home (no auth required)
adb shell am start -W \
  -a android.intent.action.VIEW \
  -d "mockdonalds://app/home"

# Profile via More tab (unauthenticated → redirects to LoginScreen with returnTo)
adb shell am start -W \
  -a android.intent.action.VIEW \
  -d "mockdonalds://app/more/profile"

# Unknown segments are silently skipped
adb shell am start -W \
  -a android.intent.action.VIEW \
  -d "mockdonalds://app/home/nonexistent/profile"
```

**iOS — via xcrun simctl:**
```bash
xcrun simctl openurl booted "mockdonalds://app/home"
xcrun simctl openurl booted "mockdonalds://app/more/profile"
```

### Unit Tests

Deep link parsing and interception are thoroughly unit tested in `composeApp/src/commonTest/`:

| Test | What it verifies |
|------|-----------------|
| `DeepLinkParserTest` | URI parsing, segment mapping, unknown segments, empty/invalid URIs |
| `AuthInterceptorTest` | Protected → rewrite to login, public → skip, authenticated → skip |
| `InterceptingNavigatorTest` | Delegation, deep link batch rewriting, mixed screen interception |

---

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

## Module Structure

Each feature follows this module layout:

```
features/<feature>/
  api/
    domain/     → Domain contracts: use case abstractions, models (no Circuit dependency)
    navigation/ → Screen object (@Parcelize), TestTags object (Circuit dependency)
  impl/
    domain/       → Use case implementations (depends on api:domain only)
    data/         → Repository implementations
    presentation/ → Presenter + UI (depends on both api:domain + api:navigation)
  test/         → Fakes/fixtures (depends on api:domain only)
```

This ensures the domain layer has zero UI framework awareness — Circuit stays in `api:navigation` and `impl:presentation`.

## Adding a New Feature/Screen

### 1. Create `api:domain` module

```
features/<feature>/api/domain/build.gradle.kts  → mockdonalds.kmp.library plugin
  dependencies: api(:core:centerpost), api(kotlinx.coroutines.core)
```

Add domain contracts:
- `Get<Feature>Content.kt` — abstract use case extending `CenterPostSubjectInteractor`
- `<Feature>Models.kt` — data classes for domain models (immutable, val only)

### 2. Create `api:navigation` module

```
features/<feature>/api/navigation/build.gradle.kts  → mockdonalds.kmp.library plugin
  dependencies: api(:core:circuit)
  # core:circuit provides circuit-runtime (Screen) + Parcelize annotation
  # If auth-protected: extend ProtectedScreen instead of Screen
```

Add:
- `<Feature>Screen.kt` — `@Parcelize object <Feature>Screen : Screen` in the `navigation` package
  - If the screen requires authentication: extend `ProtectedScreen` instead of `Screen` and add `core:circuit` dependency
- `<Feature>TestTags.kt` — `object <Feature>TestTags` with const val tags in the `ui` package

### 3. Create `impl:domain` module

- `Get<Feature>ContentImpl.kt` with `@ContributesBinding(AppScope::class)`
- `<Feature>Repository.kt` interface
- `Get<Feature>ContentImplTest.kt` in commonTest

### 4. Create `impl:data` module

- `<Feature>RepositoryImpl.kt` with `@ContributesBinding(AppScope::class)`
- `<Feature>RepositoryImplTest.kt` in commonTest

### 5. Create `impl:presentation` module

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

- `settings.gradle.kts` — **automatic**: feature modules are auto-discovered from `features/` directories with architecture-enforced submodules (api:domain, api:navigation, impl:data, impl:domain, impl:presentation, test)
- `composeApp/build.gradle.kts` — **automatic**: feature deps are auto-discovered with enforced wiring (api:domain + api:navigation as `api()`, impl:data + impl:domain as `implementation()`, impl:presentation as `api()`)
- Cross-feature navigation: depend on `<other>:api:navigation` (not full api) in the feature's impl:presentation build.gradle.kts

### 9. Verify

- `./gradlew assemble` — full build
- `./gradlew testAndroidHostTest` — unit tests
- `./gradlew :konsist:test` — architecture enforcement
- `./gradlew detektMetadataCommonMain` — lint
- `swiftlint lint iosApp` — Swift lint
- `swift test --package-path iosApp/ArchitectureCheck` — Harmonize

## Architecture Enforcement

### Konsist Rules (Kotlin)

The following rules in `CircuitConventionsTest` enforce the interception and auth patterns:

| Rule | What it enforces |
|------|-----------------|
| `Screen objects should reside in api navigation packages` | Both `Screen` and `ProtectedScreen` implementations must be in `api/navigation` |
| `Screen objects should have @Parcelize annotation` | All screens (including `ProtectedScreen`) must be `@Parcelize` for state restoration |
| `ProtectedScreen implementations should reside in api navigation packages` | `ProtectedScreen` marker is only used in `api/navigation`, not in domain or presentation |

These rules exclude test source sets (`commonTest`, `androidDeviceTest`, etc.) where test-only screen stubs are defined.

### Harmonize Rules (Swift)

iOS view tests enforce that every feature view (including `ProfileView`) has a corresponding `ViewTest`, `ViewRobot`, and `StateRobot` following the robot pattern. The `TestConventionsTest.testEveryViewHasAViewTest` check ensures new features aren't shipped without iOS UI tests.

---

## Key Files

### Navigation Core

| File | Role |
|------|------|
| `composeApp/src/iosMain/.../bridge/NavigationAction.kt` | Sealed class — navigation intent types |
| `composeApp/src/iosMain/.../bridge/BridgeNavigator.kt` | Navigator impl → StateFlow for iOS |
| `composeApp/src/iosMain/.../bridge/IosApp.kt` | Creates BridgeNavigator + InterceptingNavigator, passes to presenters |
| `composeApp/src/androidMain/.../App.kt` | Android: NavigableCircuitContent + InterceptingNavigator + deep links |
| `iosApp/iosApp/Circuit/CircuitNavigator.swift` | SwiftUI navigation host — observes bridge |
| `iosApp/iosApp/Circuit/Circuit.swift` | Exposes navigator to SwiftUI |
| `iosApp/iosApp/MockDonaldsApp.swift` | Wraps TabView with CircuitNavigator + `.onOpenURL` |

### Interception & Auth

| File | Role |
|------|------|
| `composeApp/src/commonMain/.../navigation/NavigationInterceptor.kt` | `InterceptResult` sealed class + `NavigationInterceptor` interface |
| `composeApp/src/commonMain/.../navigation/InterceptingNavigator.kt` | Navigator wrapper that runs interceptor chain |
| `composeApp/src/commonMain/.../navigation/AuthInterceptor.kt` | Redirects `ProtectedScreen` to login when unauthenticated |
| `composeApp/src/commonMain/.../navigation/DeepLinkParser.kt` | URI → `List<Screen>` parser |
| `core/circuit/src/commonMain/.../core/circuit/ProtectedScreen.kt` | Marker interface for auth-gated screens |
| `core/auth/api/src/commonMain/.../core/auth/AuthManager.kt` | Auth state interface |
| `core/auth/impl/src/commonMain/.../core/auth/InMemoryAuthManager.kt` | In-memory auth implementation |

### Deep Link Entry Points

| File | Role |
|------|------|
| `androidApp/src/main/AndroidManifest.xml` | Intent filter for `mockdonalds://app/*` |
| `androidApp/.../MainActivity.kt` | Passes `intent` to `MockDonaldsApp` |
| `iosApp/iosApp/MockDonaldsApp.swift` | `.onOpenURL` handler |
| `iosApp/iosApp/AppDelegate.swift` | `handleDeepLink(url:)` → Kotlin `iosApp.deepLink(uri:)` |
