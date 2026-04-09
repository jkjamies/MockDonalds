---
name: verify
description: Run the full verification pipeline — build, lint, unit tests, architecture tests, navigation/integration tests, and e2e tests. Use after any code changes to ensure nothing is broken.
---

# Verify

Run the complete verification pipeline. Every step must pass before work is considered complete.

## Steps

Run these commands in order. Stop and fix any failures before proceeding to the next step.

### 1. Build
```bash
./gradlew assemble
```

### 2. Lint — Detekt
```bash
./gradlew detektMetadataCommonMain
```

### 3. Lint — SwiftLint
```bash
swiftlint --config .swiftlint.yml
```

### 4. Unit Tests (Kotlin)
```bash
./gradlew testAndroidHostTest
```

### 5. Unit Tests (iOS — requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Runs 42 iOS unit tests (ViewTests with ViewInspector Robot pattern). Skip if no simulator is available.

### 6. Architecture Tests (Konsist)
```bash
./gradlew :architecture-check:test
```
22 test classes enforce: layer dependencies, naming conventions, DI annotations, forbidden patterns, test coverage requirements, and more.

### 7. Architecture Tests (iOS — Harmonize)
```bash
swift test --package-path iosApp/ArchitectureCheck
```
33 tests enforce: Swift view conventions, test module organization, navint test conventions, iOS architectural patterns.

### 8. Navigation & Integration Tests (requires emulator)
```bash
./gradlew :navint-tests:connectedAndroidDeviceTest
```
Runs on a connected Android emulator. Tests use real Circuit presenters with a fake data layer (no impl/domain or impl/data). Test files end with `NavigationTest` or `IntegrationTest` and use JUnit4 `@RunWith(AndroidJUnit4::class)`. Skip this step if no emulator is available; run it before merge when presentation or navigation modules changed.

### 9. E2E Tests (requires device/emulator)
```bash
./gradlew :e2e-tests:connectedAndroidTest
```
Runs full user journey tests and startup benchmarks against the real app via UI Automator. Tests use `AppRobot` for launch, tab navigation, deep links, and element assertions. Journey tests end with `JourneyTest`, benchmarks end with `Benchmark`. Skip this step if no device/emulator is available; run it before merge when user-facing flows or deep link handling changed.

### 10. iOS Navigation & Integration Tests (requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Runs 24 navint tests on an iOS Simulator. Tests use Swift Testing (`@Suite @MainActor struct`) and exercise `NavigationStateManager` state transitions, tab switching, deep link navigation, and auth flow navigation. Skip this step if no simulator is available; run it before merge when Swift navigation files changed (`iosApp/iosApp/Circuit/` or `iosApp/iosAppTests/NavInt/`).

## Interpreting Failures

- **Build failure**: Check import paths, missing dependencies, or syntax errors
- **Detekt failure**: Code style violation — check the reported rule and file:line
- **SwiftLint failure**: Swift code style violation — check the reported rule and file:line
- **Unit test failure**: Logic error in implementation or test setup
- **Konsist failure**: Architecture rule violation — the error message names the specific rule (e.g., "Presenters must not depend on repositories directly")
- **Harmonize failure**: iOS architecture convention violated — check Swift view/test naming patterns
- **navint-tests failure**: Navigation or integration contract broken — check Circuit presenter wiring, screen navigation flows, and fake data layer setup in `navint-tests/src/androidDeviceTest/`
- **e2e-tests failure**: User journey broken — check `AppRobot.kt` for test helper, journey tests in `e2e-tests/src/main/kotlin/.../suites/`, and TestTags in `features/*/api/navigation/`
- **iOS navint-tests failure**: iOS navigation state management broken — check `NavigationStateManager` logic in `iosApp/iosApp/Circuit/`, tab switching, deep link routing, or auth flow handling in `iosApp/iosAppTests/NavInt/`
