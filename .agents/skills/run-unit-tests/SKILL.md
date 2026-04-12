---
name: run-unit-tests
description: Run pure-logic unit tests — Kotlin (Kotest on JVM host) and Swift (Swift Testing in iosAppTests/Unit/). Use to validate non-UI logic changes. For iOS view tests, use run-ui-tests instead.
---

# Run Unit Tests

## When to Use

After modifying any Kotlin source files in `impl/domain`, `impl/data`, or `impl/presentation` modules, or Swift pure-logic files (helpers, formatters, `NavigationStateManager` state transforms) in `iosApp/iosApp/`.

**Not for iOS view tests** — those live in `iosApp/iosAppTests/UIComponent/` and are run via the `run-ui-tests` skill against the `UIComponentTests` test plan.

## Run All Unit Tests

### Kotlin (runs on JVM host — no device needed)
```bash
./gradlew testAndroidHostTest
```

### iOS pure-logic (requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Runs the Swift Testing tests in `iosApp/iosAppTests/Unit/`. Currently holds a single `PlaceholderUnitTest` (`1 + 1 == 2`) — the plumbing is wired so real iOS-side pure-logic tests drop into that directory without any build-system work. Most business logic lives in KMP Kotlin and is covered by Kotest above.

## Run Tests for a Specific Feature

```bash
# Domain tests
./gradlew :features:{name}:impl:domain:testAndroidHostTest

# Data tests
./gradlew :features:{name}:impl:data:testAndroidHostTest

# Presenter tests
./gradlew :features:{name}:impl:presentation:testAndroidHostTest
```

Valid feature names: `home`, `login`, `more`, `order`, `profile`, `rewards`, `scan`

## Run Tests for a Specific Core Module

```bash
./gradlew :core:{module}:testAndroidHostTest
```

Valid core modules: `auth:api`, `auth:impl`, `centerpost`, `circuit`, `network`, `theme`, `test-fixtures`

## Interpreting Results

- Tests use Kotest `BehaviorSpec` (Given/When/Then structure)
- Test failures show the spec name and assertion that failed
- All tests use `TestCenterPostDispatchers` for deterministic coroutine execution
- Tests use fakes (not mocks) — check `features/{name}/test/` for fake implementations

## Out of Scope

Navigation and integration tests in `testing/navint-tests/` are **not** unit tests. They run on a connected Android emulator via `./gradlew :testing:navint-tests:connectedAndroidDeviceTest` and use JUnit4 `@RunWith(AndroidJUnit4::class)` rather than Kotest BehaviorSpec. Do not include them in the unit test run.
