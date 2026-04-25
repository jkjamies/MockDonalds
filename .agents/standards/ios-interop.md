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
│  ├── Pop                         ├── Run-loop batching via          │
│  ├── ResetRoot(screen)           │    dispatch_async(main_queue)    │
│  ├── SwitchTab(tag)              └── Detects FlowScreen in goTo()  │
│  ├── DeepLink(screens)                → emits PresentFlow           │
│  ├── PresentFlow(screen)                                            │
│  └── DismissFlow                                                    │
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
│  ├── Presents FlowScreen as .fullScreenCover with inner NavStack   │
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
4. **NavigationAction** — sealed class with GoTo, Pop, ResetRoot, SwitchTab, DeepLink, PresentFlow, DismissFlow subtypes.
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

## Flow Navigation (Nested Flows)

Screens implementing `FlowScreen` (from `core:circuit`) are presented as modal flows on iOS. When `BridgeNavigator.goTo()` receives a `FlowScreen`, it emits `PresentFlow` instead of `GoTo`. `NavigationStateManager` handles this by:
- Setting `flowRootScreen` and presenting a `.fullScreenCover`
- Routing subsequent `GoTo`/`Pop` actions to `flowPath` (the flow's inner NavigationStack)
- Dismissing the flow when `Pop` is called on an empty `flowPath`

Inner flow screens are regular Circuit screens with their own presenters — no special handling needed. Their `navigator.goTo()`/`pop()` calls go through the main `BridgeNavigator` and get routed to the flow's inner path because `isFlowActive` is true.

On Android, `FlowScreen` has no special navigation behavior. The flow screen is pushed onto the main backstack normally, and its Compose UI uses Circuit's nested `CircuitContent(onNavEvent)` to manage inner screen navigation — a purely UI-layer concern.

`ResetRoot`, `SwitchTab`, and `DeepLink` actions dismiss any active flow as a safety measure.

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

Plus `core:circuit` for shared Circuit types (TabScreen, ProtectedScreen), and `core:feature-flag:impl` for the `HarnessIosBridge` contract (see "Native Swift bridge pattern" below).

## Native Swift bridge pattern

Used when an iOS vendor SDK has no pure-Kotlin binding and we want to avoid CocoaPods and `expect/actual` FFI. Precedent: Harness Feature Flags (`core:feature-flag:impl`).

**Contract**: a Kotlin interface in the core module's `iosMain` (e.g., `HarnessIosBridge`). Listener callbacks are bridged to `Flow` via `callbackFlow` in a Kotlin impl that depends on the interface.

**Swift side** is split across two locations because a local SPM package **cannot import the `ComposeApp` framework** (that framework is produced by Gradle's `embedAndSignAppleFrameworkForXcode` and linked into the `iosApp` Xcode target only — SPM packages are resolved before it exists):

- **SPM package** colocated in the core module under `{module}/impl/swift/` — owns the vendor SDK dependency and all vendor-specific work (SDK init, variation reads, listener registration). Exposes a Swift-native public API (e.g., `HarnessClient`). **Does not import `ComposeApp`.**
- **iosApp target adapter** (e.g., `iosApp/iosApp/Harness/SwiftHarnessBridge.swift`) — a thin class that imports both `ComposeApp` (for the Kotlin interface) and the SPM product, conforms to the Kotlin interface, and delegates to the SPM client. This is the only place where Swift code crosses the Kotlin boundary.

**Wiring**:
- Export the core module from the ComposeApp framework (`export(project(":core:xxx:impl"))`) and switch its dependency to `api(...)` in `composeApp/commonMain`, so Swift can reach the interface.
- Add the local package to `iosApp/project.yml` under `packages:` (`path: ../core/{module}/impl/swift`) and list its product under `targets.iosApp.dependencies`.
- Pass the Swift adapter instance into Kotlin via a `@DependencyGraph.Factory` on the iOS-specific `ProdAppGraph` (see "Per-platform AppGraph" below).

**Keep the bridge minimal**. It should expose only what the Kotlin-side `RemoteFeatureFlagSource` (or equivalent abstraction) needs — not the vendor SDK's surface.

## Per-platform AppGraph

`ProdAppGraph` is platform-specific, not shared:

| Source set | Factory parameter | Purpose |
|------------|-------------------|---------|
| `composeApp/androidMain/AppGraph.kt` | `@Provides Application` | Android SDK init (e.g., Harness `CfClient.initialize(context, …)`) |
| `composeApp/iosMain/AppGraph.kt` | `@Provides HarnessIosBridge` (and any other Swift-provided bridges) | Injecting Swift-owned instances into the Kotlin graph |

Callers use `createGraphFactory<ProdAppGraph.Factory>().create(…)` rather than `createGraph<ProdAppGraph>()`. This pattern generalizes whenever either platform needs host-owned types (Application, Swift bridges) as DI inputs.

## ScreenUiFactory Registration via `@CircuitInject` (Swift macro)

`AppDelegate` consumes a generated factory list — authors do **not** edit `AppDelegate.swift`
to register new screens. Instead, every SwiftUI view annotates itself with `@CircuitInject`,
mirroring Kotlin's presenter/UI registration on Android:

```swift
import CircuitMacros

@CircuitInject(HomeScreen.self, HomeUiState.self)
struct HomeView: View {
    let state: HomeUiState
    var body: some View { … }
}
```

A pre-build script scans `iosApp/**/*.swift`, extracts every `@CircuitInject` site, and emits
`iosApp/iosApp/Generated/GeneratedCircuitFactories.swift`, an extension on `CircuitIos` that
exposes `static func generatedFactories() -> [UiFactory]`. `AppDelegate` wires it as:

```swift
return CircuitIos(iosApp: iosApp, uiFactories: CircuitIos.generatedFactories())
```

**Two SPM packages back this:**

| Package | Location | Role |
|---------|----------|------|
| `CircuitMacros` | `iosApp/CircuitMacros/` | Declares `@CircuitInject` (peer macro). Consumed by the iosApp Xcode target. iOS-only platforms array; the macro plugin builds for the host (macOS) automatically. |
| `CircuitFactoryRegistry` | `build-tooling/CircuitFactoryRegistry/` | Executable that parses Swift source via SwiftSyntax and emits the generated factories file. macOS-only, deliberately outside `iosApp/` so Xcode's SwiftPM driver doesn't try to load its manifest. |

The peer macro returns `[]` — its purpose is compile-time type validation (typos in
`HomeScreen.self`/`HomeUiState.self` fail the Swift build). The registry executable does the
actual codegen.

**`@CircuitInject` annotations honour `#if DEBUG`.** The registry visitor tracks `#if DEBUG`
nesting and emits debug entries inside an `#if DEBUG` block in the generated file, matching
the source's gating exactly. Debug-only screens (`DebugMenuView`, `BuildConfigDebugView`,
`FeatureFlagsDebugView`) keep their file-level `#if DEBUG` and the registry mirrors that.

**Build-script env hygiene.** The pre-build script runs `swift build` on the registry
executable. Xcode's iOS build environment exports `SDKROOT=iphonesimulator`, which would
leak into SwiftPM's macOS manifest evaluator and produce the cryptic
`error: 'circuitfactoryregistry': Invalid manifest`. The script `unset`s `SDKROOT`,
`PLATFORM_NAME`, `EFFECTIVE_PLATFORM_NAME`, `TARGET_DEVICE_PLATFORM_NAME`, and `TOOLCHAINS`
before invoking `swift build`. Any future "build a host tool from an iOS run script" must do
the same.

See `iosApp/CircuitMacros/AGENTS.md` and `build-tooling/CircuitFactoryRegistry/AGENTS.md`
for the macro plugin internals and registry tool internals respectively.

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
