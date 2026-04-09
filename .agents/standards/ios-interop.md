# iOS Interop — KMP-to-Swift Contract

## Compose Runtime, Not Compose UI

**iOS uses the Compose _runtime_ for state management only — not Compose UI for rendering.**

This is the most important architectural distinction in the project. On iOS, the entire UI is native SwiftUI. Compose is used solely as a reactive state engine via [Molecule](https://github.com/cashapp/molecule), which runs `@Composable` presenter functions and converts their output to `StateFlow`. SwiftUI views observe this flow and render natively.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           ANDROID                                       │
│                                                                         │
│  Presenter.present()                                                    │
│       │                                                                 │
│       ▼                                                                 │
│  ┌─────────────────────┐    ┌──────────────────────────────┐            │
│  │   Compose Runtime   │───▶│      Compose UI (Jetpack)    │            │
│  │  (state + recomp.)  │    │  TextField, Button, Column   │            │
│  └─────────────────────┘    │  Material3, Canvas, Layout   │            │
│                              └──────────────────────────────┘            │
├─────────────────────────────────────────────────────────────────────────┤
│                             iOS                                         │
│                                                                         │
│  Presenter.present()                                                    │
│       │                                                                 │
│       ▼                                                                 │
│  ┌─────────────────────┐    ┌─────────────┐    ┌───────────────────┐    │
│  │   Compose Runtime   │───▶│  Molecule    │───▶│    StateFlow      │    │
│  │  (state + recomp.)  │    │ (bridge)     │    │                   │    │
│  └─────────────────────┘    └─────────────┘    └────────┬──────────┘    │
│                                                          │              │
│                                              KMP-NativeCoroutines       │
│                                                          │              │
│                                                          ▼              │
│                                                ┌─────────────────┐      │
│                                                │  AsyncSequence   │      │
│                                                └────────┬────────┘      │
│                                                          │              │
│                                                          ▼              │
│                                                ┌─────────────────────┐  │
│                                                │    SwiftUI View     │  │
│                                                │  Text, Button, VStack│  │
│                                                │  NavigationStack    │  │
│                                                │  100% native Apple  │  │
│                                                └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### What this means in practice

| Aspect | Android | iOS |
|--------|---------|-----|
| **UI toolkit** | Compose UI (Jetpack) | SwiftUI (native Apple) |
| **State engine** | Compose Runtime | Compose Runtime (via Molecule) |
| **Rendering** | Compose render nodes | UIKit/SwiftUI render pipeline |
| **Look & feel** | Material3 | Native iOS (UIKit conventions) |
| **Animations** | Compose animation APIs | SwiftUI `.animation`, `.transition` |
| **Accessibility** | Compose semantics | SwiftUI `.accessibilityIdentifier` |
| **Navigation** | `NavigableCircuitContent` | `NavigationStack` (native) |
| **Platform feel** | Android-native | iOS-native |

### Why this matters

- **iOS views are fully native.** They use SwiftUI layout, transitions, and platform conventions. There is no Compose UI rendering on iOS — no Compose Canvas, no Compose Layout, no Compose Modifier chains. An iOS developer sees standard SwiftUI code.
- **Shared code is logic, not pixels.** Presenters, use cases, repositories, and models are shared. The Compose runtime runs presenter `@Composable` functions to produce state. Everything after that is platform-native.
- **Both platforms get first-class UX.** Android gets Material3 with Compose UI. iOS gets native SwiftUI with iOS design language. Neither platform compromises its feel for the other.
- **Molecule is the bridge, not a renderer.** Molecule takes a `@Composable () -> T` function and produces a `StateFlow<T>`. It uses the Compose runtime's recomposition engine for reactive state, but never touches rendering. Think of it as "Compose without the UI."

### The data flow

```
Shared (Kotlin):  Repository → UseCase → Presenter.present() → UiState
                                                    │
                        ┌───────────────────────────┤
                        │                           │
Android:    Compose Runtime → Compose UI        Molecule → StateFlow
                                                    │
iOS:                                    KMP-NativeCoroutines → AsyncSequence
                                                    │
                                              SwiftUI View
```

The `@Composable present()` function is the same code on both platforms. The divergence happens at the output: Android feeds `UiState` into Compose UI nodes; iOS feeds it through Molecule → StateFlow → AsyncSequence into SwiftUI views.

## sealed class vs sealed interface

Events MUST be `sealed class` (not `sealed interface`) for iOS interop. Kotlin sealed classes
export to Obj-C as a class hierarchy — Swift can instantiate subtypes directly via
`Event.Subtype()` syntax. Sealed interfaces export as Obj-C protocols, which prevents
exhaustive switching and breaks the `Event.Subtype()` constructor pattern Swift relies on.

This is enforced by Konsist's `CircuitConventionsTest`.

## Circuit Bridge Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                        Kotlin (iosMain)                              │
│                                                                      │
│  IosApp                          CircuitPresenterKotlinBridge        │
│  ├── createGraph<ProdAppGraph>()     ├── Wraps @Composable present()    │
│  ├── circuit: Circuit            ├── Molecule → StateFlow           │
│  ├── navigator: BridgeNavigator  └── @NativeCoroutinesState         │
│  └── presenterBridge(screen)                                        │
│                                  BridgeNavigator                     │
│  NavigationAction (sealed)       ├── Implements Circuit Navigator   │
│  ├── Idle                        ├── Channel<List<NavigationAction>> │
│  ├── GoTo(screen)                ├── @NativeCoroutines              │
│  ├── Pop                         └── Run-loop batching via          │
│  ├── ResetRoot(screen)                dispatch_async(main_queue)    │
│  ├── SwitchTab(tag)                                                 │
│  └── DeepLink(screens)                                              │
└──────────────────────┬───────────────────────┬───────────────────────┘
                       │                       │
┌──────────────────────▼───────────────────────▼───────────────────────┐
│                        Swift (iosApp)                                │
│                                                                      │
│  CircuitIos          CircuitView            CircuitContent           │
│  ├── uiFactories     ├── Observes stateFlow ├── Resolves screen     │
│  ├── presenterBridge  ├── via asyncSequence  ├── Finds UI factory   │
│  ├── navigator        └── Renders SwiftUI   └── Creates view        │
│  └── shared singleton                                               │
│                                                                      │
│  CircuitNavigator                                                    │
│  ├── Observes navigationActions via asyncSequence                   │
│  ├── Drives NavigationStack (GoTo → push, Pop → pop)               │
│  ├── Processes batched actions sequentially in one update cycle     │
│  ├── ScreenEntry wrapper (Hashable + Identifiable)                  │
│  └── Calls consume() after handling each batch                      │
│                                                                      │
│  ScreenUiFactory<S, State> { view }   ← One-liner per screen       │
│  CircuitStack                         ← EnvironmentObject provider  │
└──────────────────────────────────────────────────────────────────────┘
```

The iOS bridge lives in `composeApp/src/iosMain/kotlin/com/mockdonalds/app/bridge/`:

1. **IosApp** — entry point. Creates `ProdAppGraph`, `BridgeNavigator`, `InterceptingNavigator`
   with `AuthInterceptor`. Exposes `presenterBridge(screen:)` and `deepLink(uri:)` to Swift.
2. **BridgeNavigator** — `Navigator` implementation using `Channel<List<NavigationAction>>`,
   NOT StateFlow. Channel provides exactly-once, ordered delivery with no replay or conflation.
   StateFlow would conflate rapid `pop()` + `goTo()` into a single emission, dropping the pop.
3. **Run-loop batching** — synchronous navigator calls within one event handler accumulate in
   a `pending` list. The first call schedules `dispatch_async(dispatch_get_main_queue())` to
   flush on the next main run loop tick. All actions arrive as a single
   `List<NavigationAction>` batch, processed in one SwiftUI update cycle.
4. **NavigationAction** — sealed class with GoTo, Pop, ResetRoot, SwitchTab, DeepLink subtypes.
5. **CircuitPresenterKotlinBridge** — wraps a Circuit `Presenter` into a `StateFlow` via
   Molecule's `launchMolecule(RecompositionMode.Immediate)`.

### State Observation Flow (iOS)

```
SwiftUI View (.task)
  └── asyncSequence(for: presenter.stateFlow)
        └── for try await state in sequence { self.state = state }
```

### Navigation Observation Flow (iOS)

```
CircuitNavigator (.task)
  └── asyncSequence(for: circuit.navigator.navigationActions)
        └── for try await batch in sequence {
              for action in batch { handleAction(action) }
              consume()  // resets channel, prevents replay
            }
```

Task cancellation is automatic when the view disappears.

## @NativeCoroutinesState for StateFlow Bridging

`CircuitPresenterKotlinBridge.state` is annotated with `@NativeCoroutinesState` (KMP-NativeCoroutines).
Swift observes it via `asyncSequence(for: presenter.stateFlow)` inside a `.task {}` modifier.
`BridgeNavigator.navigationActions` uses `@NativeCoroutines` (not State) because it is a Flow, not StateFlow.

## SwiftUI View Conventions

Every feature View must:
- `import ComposeApp` to access shared KMP types
- Accept a shared `UiState` as its `state` property
- Be stateless — call `state.eventSink` for user actions, never hold local business state
- Use `accessibilityIdentifier` with shared `*TestTags` from KMP api/navigation modules

## TabScreen Tags

`TabScreen.tag` (from `core/circuit`) is the single source of truth for tab identification
across Android Compose, iOS Kotlin bridge, and SwiftUI. The Swift `CircuitNavigator` uses
`selectedTab` bound to these tags. `BridgeNavigator.switchTab(tag:)` emits `SwitchTab` actions.

## TestTags Shared via KMP

TestTags objects live in `features/{name}/api/navigation/` and are exported to Swift via the
iOS framework. Both Android UI tests (`onNodeWithTag`) and iOS tests (`accessibilityIdentifier`)
use the same tag constants.

## iOS Framework Export Requirements

In `composeApp/build.gradle.kts`, the iOS framework auto-exports for each feature:
- `api:domain` — models, abstract use cases
- `api:navigation` — Screen objects, TestTags
- `impl:presentation` — UiState, Event sealed classes, presenter types

Plus `core:circuit` for shared Circuit types (TabScreen, ProtectedScreen).

## ScreenUiFactory Registration (AppDelegate.swift)

`AppDelegate` creates `CircuitIos` with an array of `ScreenUiFactory<Screen, UiState>` entries:
```swift
ScreenUiFactory<HomeScreen, HomeUiState> { HomeView(state: $0) }
```
Each factory matches on Screen type and casts the state. Adding a new screen requires a new
`ScreenUiFactory` entry in `AppDelegate.swift`.

## iOS Robot Pattern Differences

| Aspect | Android | iOS |
|--------|---------|-----|
| UI testing | Compose UI Test (`createComposeRule`) | ViewInspector |
| Test framework | Kotest BehaviorSpec | Swift Testing (`@Suite`, `@Test`) |
| Concurrency | Kotest coroutine support | `@MainActor` on ViewRobots |
| Robot visibility | Regular classes | `@MainActor final class` |
| Test structs | N/A (Kotest classes) | `@Suite @MainActor struct` |

StateRobots extend `BaseStateRobot<State, Event>`. ViewRobots compose a StateRobot.
ViewTests only interact through ViewRobots (never direct state manipulation).

## Common Gotchas

- **BridgeNavigator batching**: `dispatch_async` aligns with the main run loop. Without it,
  `pop()` + `goTo()` produce two SwiftUI updates with a visible "pop flash".
- **`.id(entry.id)`**: Required on `CircuitContent` inside `navigationDestination` so SwiftUI
  treats each `ScreenEntry` as a unique view identity, even for the same Screen type.
- **Nullable KMP types in Swift**: Kotlin `T?` exports as Swift optional, but generic bounds
  like `CircuitUiState` require explicit `any` protocol syntax (`any Circuit_runtimeCircuitUiState`).
- **Force casts in Circuit bridge**: `state as! State` in `ScreenUiFactory` is unavoidable for
  KMP interop — SwiftLint excludes `iosApp/iosApp/Circuit/` for this reason.
