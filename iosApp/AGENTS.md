# iOS App (SwiftUI Shell)

## Project generation (xcodegen)

`iosApp.xcodeproj/project.pbxproj` is a **generated artifact**. The source of truth is `iosApp/project.yml`, consumed by [xcodegen](https://github.com/yonaskolb/XcodeGen).

**Why xcodegen.** The matrix of 5 markets × 3 envs × 2 build types = 30 build configurations × 4 targets = **120 `XCBuildConfiguration` entries** (plus 30 at project level). Hand-editing `project.pbxproj` at this scale is error-prone and produces unreviewable PR diffs. `project.yml` is ~260 lines of declarative YAML; adding a market adds ~12 lines total.

**Install:** `brew install xcodegen` (project requires v2.45+).

**Regenerate:** `cd iosApp && xcodegen generate`. Takes <1s. Run this after editing `project.yml`, adding/removing xcconfig files, or adding Swift source files in a new top-level directory.

**Commit both `project.yml` and `project.pbxproj`** in the same commit — CI and team members who don't run xcodegen locally need the generated project to open in Xcode.

**What's in `project.yml`:**

| Section | What it defines |
|---|---|
| `configs:` | All 30 build configurations (`US-Int-Debug`, `AU-Prod-Release`, etc.) mapped to `debug`/`release` |
| `targets.iosApp` | Application target: sources (iosApp/, Assets.xcassets), Info.plist, ComposeApp framework linkage, pre-build Gradle script, per-config xcconfig refs |
| `targets.iosAppTests` | Unit-test bundle (Swift Testing + ViewInspector), depends on `iosApp` |
| `targets.iosAppE2ETests` | UI-test bundle (XCUITest journeys), depends on `iosApp` |
| `targets.iosAppBenchmarks` | UI-test bundle (XCTApplicationLaunchMetric), depends on `iosApp` |
| `schemes.iOSApp` | Single shared scheme; Run/Test/Analyze → `US-Int-Debug`, Profile/Archive → `US-Prod-Release`; 6 test plans wired |

**Never edit `project.pbxproj` directly.** If the pbxproj diverges from what xcodegen produces, CI regeneration will wipe the manual changes. If you need a setting xcodegen doesn't cover, put it in an xcconfig (for per-config values) or extend `project.yml` (for target-level structure).

**Adding a new market:** see `.agents/standards/markets.md` for the cross-cutting market concept, `.agents/standards/build-config.md` § "Adding a new market" for the mechanics, and the `add-market` skill for the automated recipe. The iOS-side short version: create 6 xcconfigs in `Configuration/{market}/`, add one `configs:` entry per build config (6 new), add 6 `configFiles:` entries under each of the four targets, then `xcodegen generate`.

## Architecture

The iOS app is a thin SwiftUI shell that consumes shared KMP presenters from the `ComposeApp` framework. There is no business logic in Swift -- all state management lives in shared Kotlin Circuit presenters.

### Entry Point

- `MockDonaldsApp.swift` -- `@main` App struct with TabView wired to KMP Screen objects
- `AppDelegate.swift` -- Creates `CircuitIos` with `IosApp` (from KMP) and consumes `CircuitIos.generatedFactories()` — the per-screen `ScreenUiFactory` list is auto-generated, no hand-maintained registration table here
- `Generated/GeneratedCircuitFactories.swift` -- regenerated on every build by the `CircuitFactoryRegistry` Run Script Phase. Walks every `*.swift` under `iosApp/iosApp/`, finds SwiftUI views annotated with `@CircuitInject(Screen.self, UiState.self)`, and emits an `extension CircuitIos { static func generatedFactories() -> [UiFactory] }`. Tracks `#if DEBUG` nesting so debug-only screens stay debug-only.
- Deep links handled via `.onOpenURL` -> `AppDelegate.handleDeepLink` -> `IosApp.deepLink(uri:)`

### `@CircuitInject` (Swift macro)

Each SwiftUI feature view declares its Screen → UiState mapping at the call site:

```swift
import CircuitMacros

@CircuitInject(HomeScreen.self, HomeUiState.self)
struct HomeView: View {
    let state: HomeUiState
    var body: some View { /* ... */ }
}
```

The macro itself is a peer macro returning `[]` — its sole job is compile-time validation that the referenced KMP `Screen` / `UiState` types exist. The actual factory wiring is done by `CircuitFactoryRegistry`, a SwiftSyntax-based codegen tool that scans the source tree at build time. See `.agents/standards/ios-interop.md` for full details.

| Package | Location | Purpose |
|---|---|---|
| `CircuitMacros` | `iosApp/CircuitMacros/` | The `@CircuitInject` peer macro (SwiftSyntax macro plugin); imported by every feature view |
| `CircuitFactoryRegistry` | `build-tooling/CircuitFactoryRegistry/` | Build-time codegen executable that scans for `@CircuitInject` sites and emits `GeneratedCircuitFactories.swift` |

The registry lives outside `iosApp/` so its macOS-targeted SwiftPM manifest doesn't get auto-discovered as an iOS dependency. See each package's `AGENTS.md` for internals.

### Circuit Bridge (iosApp/Circuit/)

Four SwiftUI components bridge Circuit to native UI:

| File | Purpose |
|------|---------|
| `CircuitStack.swift` | Root wrapper that injects `CircuitIos` as `@EnvironmentObject` and sets dark color scheme |
| `CircuitNavigator.swift` | Observes `BridgeNavigator` navigation actions, delegates to `NavigationStateManager`, drives `NavigationStack`. Presents `FlowScreen` destinations via `.fullScreenCover` with an inner `NavigationStack` for nested flow navigation. |
| `NavigationStateManager.swift` | Testable navigation state: handles GoTo/Pop/ResetRoot/SwitchTab/DeepLink/PresentFlow/DismissFlow actions. Manages `navigationPath`, `selectedTab`, and flow state (`flowRootScreen`, `flowPath`, `isFlowActive`). When a flow is active, GoTo/Pop route to the flow's inner path. |
| `CircuitView.swift` | Observes a `CircuitPresenterKotlinBridge` state flow via KMP-NativeCoroutines `asyncSequence`, renders content when state arrives |
| `CircuitContent.swift` | Takes a Screen, resolves presenter and UI factory from `@EnvironmentObject CircuitIos`, renders via `CircuitView` |

### Feature Views (iosApp/Features/)

One SwiftUI View per feature: `HomeView`, `OrderView`, `RewardsView`, `ScanView`, `MoreView`, `LoginView`, `ProfileView`. Each receives a shared UiState and calls `eventSink` for user actions.

## Harmonize Architecture Tests

Swift-side equivalent of Konsist. Located in `iosApp/ArchitectureCheck/` as a Swift Package (Harmonize 0.9.0).

| Test File | What It Enforces |
|-----------|-----------------|
| `ViewConventionsTest.swift` | Views conform to View protocol, import ComposeApp, have `state` property, use accessibilityIdentifier with shared TestTags, no UIKit/Combine/DispatchQueue, no force unwraps/casts/try, no print/TODO/FIXME |
| `TestConventionsTest.swift` | Every View has ViewTest/ViewRobot/StateRobot. Robot pattern encapsulation (ViewTest only uses ViewRobot). StateRobots extend BaseStateRobot. ViewTests are `@Suite` structs with `@Test` methods (Swift Testing, not XCTest). ViewRobots are `@MainActor final` classes importing ViewInspector. Landscape test coverage required. NavInt tests must be `@Suite @MainActor struct`. E2E journeys in `iosAppE2ETests/Suites/` must extend XCTestCase, use AppRobot, and end with JourneyTest. Benchmarks in `iosAppBenchmarks/` must end with PerformanceTest or Benchmark. |

Run Harmonize tests:
```bash
cd iosApp/ArchitectureCheck && swift test
```

## Navigation & Integration Tests (NavInt)

Tests for iOS-native navigation infrastructure in `iosAppTests/NavInt/`. These test the `NavigationStateManager` which handles all navigation actions from Kotlin's `BridgeNavigator`.

### Test Directory Organization

```
iosAppTests/
  Unit/           — Feature view tests (Robot pattern)
    Home/, Login/, More/, Order/, Profile/, Rewards/, Scan/
    StateRobot.swift  — BaseStateRobot protocol
  NavInt/         — Navigation + integration tests
    Navigation/   — NavigationStateManagerTest, TabSwitchingTest, DeepLinkNavigationTest
    Integration/  — AuthFlowNavigationTest
```

| Directory | Tests |
|-----------|-------|
| `Unit/{Feature}/` | `{Feature}ViewTest`, `{Feature}ViewRobot`, `{Feature}StateRobot` |
| `NavInt/Navigation/` | `NavigationStateManagerTest` (push/pop/reset/batch), `TabSwitchingTest`, `DeepLinkNavigationTest` |
| `NavInt/Integration/` | `AuthFlowNavigationTest` (auth redirect, post-login navigation) |

Uses Swift Testing (`@Suite @MainActor struct`), same as unit tests. No ViewInspector needed — tests exercise the state manager directly.

### Test Plans

Tests are separately invokable via Xcode test plans:

| Test Plan | Scope | Command |
|-----------|-------|---------|
| `AllTests` (default) | Every test in `iosAppTests` | `xcodebuild test -scheme iOSApp -testPlan AllTests` |
| `UnitTests` | Pure-logic Swift Testing tests in `iosAppTests/Unit/` (PlaceholderUnitTest today; reserved for Swift-only helpers) | `xcodebuild test -scheme iOSApp -testPlan UnitTests` |
| `UIComponentTests` | ViewInspector Robot-pattern view tests in `iosAppTests/UIComponent/` | `xcodebuild test -scheme iOSApp -testPlan UIComponentTests` |
| `NavIntTests` | Navigation + integration tests in `iosAppTests/NavInt/` | `xcodebuild test -scheme iOSApp -testPlan NavIntTests` |
| `E2ETests` | End-to-end journeys in `iosAppE2ETests/Suites/` (debug target) | `xcodebuild test -scheme iOSApp -testPlan E2ETests` |
| `Benchmarks` | Launch-time performance tests in `iosAppBenchmarks/` (release-like target) | `xcodebuild test -scheme iOSApp -testPlan Benchmarks` |

## E2E Tests (XCUITest)

Process-isolated end-to-end journey tests in `iosAppE2ETests/`. These launch the real app via XCUITest and interact through accessibility identifiers (shared TestTags from KMP). Target symmetry: Gradle `:testing:mobile:e2e-tests` ↔ Xcode `iosAppE2ETests`.

| Directory | Tests |
|-----------|-------|
| `Suites/` | `GuestJourneyTest`, `DeepLinkJourneyTest`, `OrderJourneyTest` |
| `Robots/` | `AppRobot` — launch, navigate tabs, assert/tap elements |

```bash
xcodebuild test -scheme iOSApp -testPlan E2ETests -destination 'platform=iOS Simulator,name=iPhone 16'
```

Key conventions:
- Uses XCTest (`XCTestCase`), NOT Swift Testing — XCUITest requires process isolation
- All element queries use accessibility identifiers (string values matching KMP TestTags)
- Journey tests use `AppRobot` exclusively for app interaction
- No model construction — interact via UI only

## Benchmarks (XCTApplicationLaunchMetric)

Launch-time performance measurements in `iosAppBenchmarks/`. Separate target/test plan from journeys so performance runs stay isolated from functional correctness. Target symmetry: Gradle `:testing:mobile:benchmarks` ↔ Xcode `iosAppBenchmarks`.

| File | Measures |
|------|----------|
| `StartupPerformanceTest.swift` | `testColdStartup` (app launch metric), `testColdStartupToFirstScreen` (cold launch until `HomeUserName` accessibility identifier is visible) |

```bash
xcodebuild test -scheme iOSApp -testPlan Benchmarks -destination 'platform=iOS Simulator,name=iPhone 16'
```

Key conventions:
- Uses XCTest with `measure(metrics: [XCTApplicationLaunchMetric()]) { … }`
- Class names end with `PerformanceTest` or `Benchmark` (Harmonize-enforced)
- No `AppRobot` dependency — benchmarks exercise launch only, not cross-feature flows
- Runs on simulator for convenience; CI uses a fixed device to keep numbers comparable

## SwiftLint

Config at project root `.swiftlint.yml`. Scoped to `iosApp/iosApp`, `iosApp/iosAppTests`, `iosApp/iosAppE2ETests`, and `iosApp/iosAppBenchmarks`. Excludes `Circuit/` directory (KMP interop bridging code). Opt-in rules: force_unwrapping, force_cast, force_try.

```bash
swiftlint --config .swiftlint.yml
```

## Harmonize Scope Config

`.harmonize.yaml` at project root excludes `ArchitectureCheck/**`, `.build/**`, `DerivedData/**`.

## iOS Test Pattern (Robot Pattern)

Tests follow the same robot pattern as Android:
- `BaseStateRobot<State, Event>` -- protocol + base class capturing events via `createEventSink()`
- Feature-specific `*StateRobot` (e.g., `HomeStateRobot`) extends `BaseStateRobot`, provides `defaultState()` and variant states
- `*ViewRobot` -- `@MainActor final class` composing a StateRobot, uses ViewInspector for assertions
- `*ViewTest` -- `@Suite @MainActor struct` with `@Test` methods

## Key Conventions

- All Views must import `ComposeApp` and use shared UiState/Event types from KMP
- Events are sealed classes (not interfaces) in KMP for iOS interop -- `sealed interface` exports as Obj-C protocol which breaks `Event.Subtype()` syntax in Swift
- TabScreen tags are the single source of truth for tab identification across Android, iOS Kotlin, and Swift
- Accessibility identifiers use shared `*TestTags` objects from KMP api modules
