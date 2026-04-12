---
name: run-all-tests
description: Run the full test pipeline (lint + all 5 test levels on both platforms). Mirrors the CI pipeline in verification.md minus the final `./gradlew assemble` step. Device/simulator/emulator required for steps 4+.
---

# Run All Tests

Runs every test step from `verification.md` → "Full Pipeline (CI)", in order, on both platforms. Stop and fix failures before proceeding to the next step. The only thing this skill omits from the full CI pipeline is the final `./gradlew assemble` — use the `verify-ci` skill if you need that too.

For a fast local inner loop that skips device-gated steps, use the `verify` skill. For diff-scoped runs, use `verify-smart`.

## Prerequisites

- Steps 1–6 run on host only (no device required).
- Step 4 (iOS unit tests) needs an iOS Simulator.
- Steps 7–12 need either a connected Android emulator/device or an iOS Simulator depending on platform. Skip individual steps if the required host is unavailable; flag them for pre-merge.

## Steps

### 1. Detekt (Kotlin lint)
```bash
./gradlew detektMetadataCommonMain
```

### 2. SwiftLint (Swift style)
```bash
swiftlint --config .swiftlint.yml
```

### 3. Kotest (Kotlin pure-logic unit tests, Android host)
```bash
./gradlew testAndroidHostTest
```

### 4. iOS unit tests (Swift Testing, pure-logic — requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Runs the `UnitTests` test plan (`iosApp/iosAppTests/Unit/`). Currently a single `PlaceholderUnitTest`; real iOS-side pure-logic tests drop into that directory without any plumbing work.

### 5. Konsist (Kotlin architecture)
```bash
./gradlew :testing:architecture-check:test
```

### 6. Harmonize (iOS architecture)
```bash
swift test --package-path iosApp/ArchitectureCheck
```

### 7. Android UI component tests (requires emulator)
```bash
./gradlew connectedAndroidDeviceTest
```
Compose Robot-pattern tests on per-feature presentation modules. Skip if no emulator is available.

### 8. iOS UI component tests (ViewInspector, requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan UIComponentTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Runs the `UIComponentTests` test plan (`iosApp/iosAppTests/UIComponent/` — ViewInspector Robot pattern, same conceptual level as Android Compose UI component tests). Skip if no simulator is available.

### 9. Android navint-tests (requires emulator)
```bash
./gradlew :testing:navint-tests:connectedAndroidDeviceTest
```
Navigation & integration tests using real Circuit presenters + fake data layer. Skip if no emulator is available.

### 10. iOS navint-tests (requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Swift Testing navigation state transitions, tab switching, deep links, auth flows. Skip if no simulator is available.

### 11. Android e2e-tests (requires device/emulator)
```bash
./gradlew :testing:e2e-tests:connectedAndroidTest
```
Full user journeys + startup benchmarks via UI Automator. Skip if no device/emulator is available.

### 12. iOS e2e-tests (requires simulator)
```bash
xcodebuild test -scheme iOSApp -testPlan E2ETests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Process-isolated XCUITest journey tests + startup benchmarks. Skip if no simulator is available.

## When to Use

- Before opening a PR as a thorough pre-merge check when you don't also need `./gradlew assemble`.
- When validating that every test level passes on a specific change.
- For just the fast inner loop, prefer the `verify` skill instead.
