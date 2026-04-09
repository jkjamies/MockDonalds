---
name: run-all-tests
description: Run lint, unit tests, and architecture tests (excludes UI tests and navint-tests which require a device). Use as a quick full check after code changes.
---

# Run All Tests

Runs lint, unit tests, and architecture tests sequentially. Does NOT include Android UI tests or navigation/integration tests (those require a connected device — use `run-ui-tests` or `:navint-tests:connectedAndroidDeviceTest` separately).

## Steps

Run in order. Stop and fix any failures before proceeding.

### 1. Lint — Detekt
```bash
./gradlew detektMetadataCommonMain
```

### 2. Lint — SwiftLint
```bash
swiftlint --config .swiftlint.yml
```

### 3. Unit Tests (Kotlin)
```bash
./gradlew testAndroidHostTest
```

### 4. Unit Tests (iOS — requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Runs 42 iOS unit tests (ViewTests with ViewInspector Robot pattern). Requires simulator. Skip if no simulator is available.

### 5. Architecture Tests (Konsist)
```bash
./gradlew :architecture-check:test
```

### 6. Architecture Tests (iOS — Harmonize)
```bash
swift test --package-path iosApp/ArchitectureCheck
```

### 7. Navigation & Integration Tests (requires emulator — optional)
```bash
./gradlew :navint-tests:connectedAndroidDeviceTest
```
Requires a connected Android emulator. Run this step when presentation or navigation modules have changed. Tests use real Circuit presenters with a fake data layer and JUnit4 `@RunWith(AndroidJUnit4::class)`. Skip if no emulator is available.

### 8. iOS Navigation & Integration Tests (requires simulator — optional)
```bash
xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Requires an iOS Simulator. Run this step when Swift navigation files have changed (`iosApp/iosApp/Circuit/` or `iosApp/iosAppTests/NavInt/`). Tests use Swift Testing and exercise `NavigationStateManager` state transitions, tab switching, deep links, and auth flows. Skip if no simulator is available.

### 9. E2E Tests (requires device/emulator — optional)
```bash
./gradlew :e2e-tests:connectedAndroidTest
```
Requires a connected Android device/emulator. Runs full user journey tests and startup benchmarks against the real app via UI Automator. Journey tests end with `JourneyTest`, benchmarks end with `Benchmark`. Skip if no device/emulator is available.

## When to Use

After any code changes as a quick validation before committing. For a more thorough check that includes the build step, use the `verify` skill instead. Navigation/integration tests (step 6) require an emulator and are optional during local development but should be run before merging changes to presentation or navigation modules.
