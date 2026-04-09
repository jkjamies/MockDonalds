---
name: verify-smart
description: Diff-aware verification that scopes checks to changed modules. Faster than full verify for targeted changes. Always runs architecture tests.
---

# Verify Smart

Scoped verification based on what actually changed. Faster than full `verify` but still catches architecture violations.

## Steps

### 1. Detect Changed Files

```bash
git diff origin/main...HEAD --name-only
```

If on `main` with uncommitted changes:
```bash
git diff --name-only
```

### 2. Lint

**If Kotlin source files changed:**
```bash
./gradlew detektMetadataCommonMain
```

**If Swift files changed:**
```bash
swiftlint --config .swiftlint.yml
```

### 3. Unit Tests (Kotlin)

Scope unit tests to affected modules:
```bash
# If features/home/ changed:
./gradlew :features:home:impl:domain:testAndroidHostTest
./gradlew :features:home:impl:data:testAndroidHostTest
./gradlew :features:home:impl:presentation:testAndroidHostTest

# If core/centerpost/ changed:
./gradlew :core:centerpost:testAndroidHostTest
```

### 3b. Unit Tests (iOS — if Swift view files changed)

**If `iosApp/iosApp/Features/` or `iosApp/iosAppTests/{Feature}/` changed:**
```bash
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Requires simulator. Runs 42 iOS ViewTests with ViewInspector Robot pattern.

### 4. Architecture Tests

Always run — these are fast and catch structural issues regardless of what changed:

```bash
./gradlew :architecture-check:test
swift test --package-path iosApp/ArchitectureCheck
```

### 5. Navigation & Integration Tests (if presentation or navigation changed)

**If `features/{name}/impl/presentation/` or `features/{name}/api/navigation/` changed:**
```bash
./gradlew :navint-tests:connectedAndroidDeviceTest
```
Requires a connected Android emulator. Tests use real Circuit presenters with a fake data layer. Skip if no emulator is available but flag that navint-tests should be run before merge.

### 6. iOS Navigation & Integration Tests (if Swift navigation files changed)

**If `iosApp/iosApp/Circuit/` or `iosApp/iosAppTests/NavInt/` changed:**
```bash
xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'
```
Requires an iOS Simulator. Tests exercise `NavigationStateManager` state transitions, tab switching, deep links, and auth flows. Skip if no simulator is available but flag that iOS navint-tests should be run before merge.

### 7. Build (if build files changed)

**If build.gradle.kts or settings.gradle.kts changed:**

```bash
./gradlew assemble
```

**If only markdown/documentation changed:**

No verification needed beyond architecture tests (which already ran in step 4).

### Module Mapping Reference

| Path Pattern | Module | Unit Test Task |
|-------------|--------|----------------|
| `features/{name}/impl/domain/` | impl:domain | `:features:{name}:impl:domain:testAndroidHostTest` |
| `features/{name}/impl/data/` | impl:data | `:features:{name}:impl:data:testAndroidHostTest` |
| `features/{name}/impl/presentation/` | impl:presentation | `:features:{name}:impl:presentation:testAndroidHostTest` |
| `features/{name}/api/` | api | `:features:{name}:api:domain:testAndroidHostTest` |
| `features/{name}/test/` | test fakes | Run tests for modules that use the fakes |
| `core/{module}/` | core module | `:core:{module}:testAndroidHostTest` |
| `features/{name}/impl/presentation/` or `features/{name}/api/navigation/` | navint-tests | `:navint-tests:connectedAndroidDeviceTest` (requires emulator) |
| `navint-tests/` | navint-tests | `:navint-tests:connectedAndroidDeviceTest` (requires emulator) |
| `iosApp/iosApp/Features/` | iOS unit tests | `xcodebuild test -scheme iOSApp -testPlan UnitTests ...` (requires simulator) |
| `iosApp/iosAppTests/{Feature}/` | iOS unit tests | `xcodebuild test -scheme iOSApp -testPlan UnitTests ...` (requires simulator) |
| `iosApp/iosApp/Circuit/` | iOS navint-tests | `xcodebuild test -scheme iOSApp -testPlan NavIntTests ...` (requires simulator) |
| `iosApp/iosAppTests/NavInt/` | iOS navint-tests | `xcodebuild test -scheme iOSApp -testPlan NavIntTests ...` (requires simulator) |
| `e2e-tests/` | e2e-tests | `:e2e-tests:connectedAndroidTest` (requires device/emulator) |

## When to Use

- After targeted changes to 1-2 features or modules
- During iterative development when full verify is too slow
- For documentation-only changes (architecture tests catch stale references)

For comprehensive validation (pre-merge, post-scaffolding), use `verify` instead.
