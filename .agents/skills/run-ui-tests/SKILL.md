---
name: run-ui-tests
description: Run Android UI tests for a specific feature. Requires a connected device or emulator. Use after modifying Compose UI code.
---

# Run UI Tests

## When to Use

After modifying any `Ui.kt` files in `impl/presentation/src/androidMain/`.

## Prerequisites

A connected Android device or running emulator is required.

## Run UI Tests for a Feature

```bash
./gradlew :features:{name}:impl:presentation:connectedAndroidDeviceTest
```

Valid feature names: `home`, `login`, `more`, `order`, `profile`, `rewards`, `scan`

## Run All UI Tests

```bash
./gradlew connectedAndroidDeviceTest
```

## Test Structure

UI tests follow the Robot pattern:
- `{Feature}UiTest.kt` — test cases, uses `UiRobot` only
- `{Feature}UiRobot.kt` — owns `StateRobot`, handles content setup and assertions
- `{Feature}StateRobot.kt` — provides state objects and captures events

Each test validates:
- Default portrait rendering
- Landscape rendering
- User interactions (taps, scrolls)
- Event dispatching via `eventSink`

## Interpreting Results

- Tests use `ComposeContentTestRule` with semantic matchers (`onNodeWithTag`, `onNodeWithText`)
- `TestTags` are defined in `api/navigation/{Feature}TestTags.kt`
- All UI is wrapped in `MockDonaldsTheme` + `LocalWindowSizeClass` provider
