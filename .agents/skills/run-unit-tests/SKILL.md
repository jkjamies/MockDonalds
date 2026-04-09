---
name: run-unit-tests
description: Run unit tests for Kotlin (Kotest) and iOS (Swift Testing + ViewInspector). Use to validate logic and UI changes.
---

# Run Unit Tests

## When to Use

After modifying any Kotlin source files in `impl/domain`, `impl/data`, or `impl/presentation` modules, or Swift view files in `iosApp/iosApp/Features/`.

## Run All Unit Tests

### Kotlin (runs on JVM host — no device needed)
```bash
./gradlew testAndroidHostTest
```

### iOS (requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Runs 42 iOS unit tests (ViewTests with ViewInspector Robot pattern).

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
