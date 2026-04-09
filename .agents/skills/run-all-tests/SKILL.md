---
name: run-all-tests
description: Run lint, unit tests, and architecture tests (excludes UI tests which require a device). Use as a quick full check after code changes.
---

# Run All Tests

Runs lint, unit tests, and architecture tests sequentially. Does NOT include Android UI tests (those require a connected device — use `run-ui-tests` separately).

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

## When to Use

After any code changes as a quick validation before committing. For a more thorough check that includes the build step, use the `verify` skill instead.
