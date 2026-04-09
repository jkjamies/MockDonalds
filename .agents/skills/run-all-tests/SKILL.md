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

### 3. Unit Tests
```bash
./gradlew testAndroidHostTest
```

### 4. Architecture Tests (Konsist)
```bash
./gradlew :architecture-check:test
```

### 5. Architecture Tests (iOS — Harmonize)
```bash
swift test --package-path iosApp/ArchitectureCheck
```

### 6. Navigation & Integration Tests (requires emulator — optional)
```bash
./gradlew :navint-tests:connectedAndroidDeviceTest
```
Requires a connected Android emulator. Run this step when presentation or navigation modules have changed. Tests use real Circuit presenters with a fake data layer and JUnit4 `@RunWith(AndroidJUnit4::class)`. Skip if no emulator is available.

## When to Use

After any code changes as a quick validation before committing. For a more thorough check that includes the build step, use the `verify` skill instead. Navigation/integration tests (step 6) require an emulator and are optional during local development but should be run before merging changes to presentation or navigation modules.
