---
name: run-ui-tests
description: Run Android and iOS UI tests for a specific feature. Requires a connected device/emulator (Android) or simulator (iOS). Use after modifying UI code.
---

# Run UI Tests

## When to Use

After modifying any `Ui.kt` files in `impl/presentation/src/androidMain/` or any `*View.swift` files in `iosApp/iosApp/Features/`.

## Android UI Tests

### Prerequisites

A connected Android device or running emulator is required.

### Run UI Tests for a Feature

```bash
./gradlew :features:{name}:impl:presentation:connectedAndroidDeviceTest
```

Valid feature names: `home`, `login`, `more`, `order`, `profile`, `rewards`, `scan`

### Run All Android UI Tests

```bash
./gradlew connectedAndroidDeviceTest
```

### Test Structure

UI tests follow the Robot pattern:
- `{Feature}UiTest.kt` — test cases, uses `UiRobot` only
- `{Feature}UiRobot.kt` — owns `StateRobot`, handles content setup and assertions
- `{Feature}StateRobot.kt` — provides state objects and captures events

Each test validates:
- Default portrait rendering
- Landscape rendering
- User interactions (taps, scrolls)
- Event dispatching via `eventSink`

### Interpreting Results

- Tests use `ComposeContentTestRule` with semantic matchers (`onNodeWithTag`, `onNodeWithText`)
- `TestTags` are defined in `api/navigation/{Feature}TestTags.kt`
- All UI is wrapped in `MockDonaldsTheme` + `LocalWindowSizeClass` provider

## iOS UI Tests

### Prerequisites

An iOS Simulator must be available (e.g., iPhone 16).

### Run All iOS UI Tests

```bash
xcodebuild test -project iosApp/iosApp.xcodeproj -scheme iOSApp -testPlan UIComponentTests -destination 'platform=iOS Simulator,name=iPhone 16'
```

### Test Structure

iOS tests follow the same Robot pattern:
- `{Feature}ViewTest.swift` — `@Suite @MainActor struct` with `@Test` methods
- `{Feature}ViewRobot.swift` — `@MainActor final class`, uses ViewInspector
- `{Feature}StateRobot.swift` — extends `BaseStateRobot`, provides state variants

Each test validates:
- Default portrait rendering
- Landscape rendering
- User interactions and event dispatching via `eventSink`

### Interpreting Results

- Tests use ViewInspector for real view hierarchy traversal
- Accessibility identifiers use shared `TestTags` from KMP api modules
- All views import `ComposeApp` for shared UiState/Event types
