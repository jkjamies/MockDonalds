# iOS App (SwiftUI Shell)

## Architecture

The iOS app is a thin SwiftUI shell that consumes shared KMP presenters from the `ComposeApp` framework. There is no business logic in Swift -- all state management lives in shared Kotlin Circuit presenters.

### Entry Point

- `MockDonaldsApp.swift` -- `@main` App struct with TabView wired to KMP Screen objects
- `AppDelegate.swift` -- Creates `CircuitIos` with `IosApp` (from KMP) and registers `ScreenUiFactory` mappings for all screens
- Deep links handled via `.onOpenURL` -> `AppDelegate.handleDeepLink` -> `IosApp.deepLink(uri:)`

### Circuit Bridge (iosApp/Circuit/)

Four SwiftUI components bridge Circuit to native UI:

| File | Purpose |
|------|---------|
| `CircuitStack.swift` | Root wrapper that injects `CircuitIos` as `@EnvironmentObject` and sets dark color scheme |
| `CircuitNavigator.swift` | Observes `BridgeNavigator` navigation actions, delegates to `NavigationStateManager`, drives `NavigationStack` |
| `NavigationStateManager.swift` | Testable navigation state: handles GoTo/Pop/ResetRoot/SwitchTab/DeepLink actions, manages `navigationPath` and `selectedTab` |
| `CircuitView.swift` | Observes a `CircuitPresenterKotlinBridge` state flow via KMP-NativeCoroutines `asyncSequence`, renders content when state arrives |
| `CircuitContent.swift` | Takes a Screen, resolves presenter and UI factory from `@EnvironmentObject CircuitIos`, renders via `CircuitView` |

### Feature Views (iosApp/Features/)

One SwiftUI View per feature: `HomeView`, `OrderView`, `RewardsView`, `ScanView`, `MoreView`, `LoginView`, `ProfileView`. Each receives a shared UiState and calls `eventSink` for user actions.

## Harmonize Architecture Tests

Swift-side equivalent of Konsist. Located in `iosApp/ArchitectureCheck/` as a Swift Package (Harmonize 0.9.0).

| Test File | What It Enforces |
|-----------|-----------------|
| `ViewConventionsTest.swift` | Views conform to View protocol, import ComposeApp, have `state` property, use accessibilityIdentifier with shared TestTags, no UIKit/Combine/DispatchQueue, no force unwraps/casts/try, no print/TODO/FIXME |
| `TestConventionsTest.swift` | Every View has ViewTest/ViewRobot/StateRobot. Robot pattern encapsulation (ViewTest only uses ViewRobot). StateRobots extend BaseStateRobot. ViewTests are `@Suite` structs with `@Test` methods (Swift Testing, not XCTest). ViewRobots are `@MainActor final` classes importing ViewInspector. Landscape test coverage required. |

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
| `AllTests` (default) | All tests | `xcodebuild test -scheme iOSApp -testPlan AllTests` |
| `UnitTests` | Feature ViewTests only | `xcodebuild test -scheme iOSApp -testPlan UnitTests` |
| `NavIntTests` | Navigation + integration | `xcodebuild test -scheme iOSApp -testPlan NavIntTests` |

## SwiftLint

Config at project root `.swiftlint.yml`. Scoped to `iosApp/iosApp` and `iosApp/iosAppTests`. Excludes `Circuit/` directory (KMP interop bridging code). Opt-in rules: force_unwrapping, force_cast, force_try.

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
