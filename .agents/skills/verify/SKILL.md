---
name: verify
description: Run the full verification pipeline — build, lint, unit tests, architecture tests, and iOS architecture tests. Use after any code changes to ensure nothing is broken.
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

### 4. Unit Tests
```bash
./gradlew testAndroidHostTest
```

### 5. Architecture Tests (Konsist)
```bash
./gradlew :architecture-check:test
```
18 test classes enforce: layer dependencies, naming conventions, DI annotations, forbidden patterns, test coverage requirements, and more.

### 6. Architecture Tests (iOS — Harmonize)
```bash
swift test --package-path iosApp/ArchitectureCheck
```
29 tests enforce: Swift view conventions, test module organization, iOS architectural patterns.

## Interpreting Failures

- **Build failure**: Check import paths, missing dependencies, or syntax errors
- **Detekt failure**: Code style violation — check the reported rule and file:line
- **SwiftLint failure**: Swift code style violation — check the reported rule and file:line
- **Unit test failure**: Logic error in implementation or test setup
- **Konsist failure**: Architecture rule violation — the error message names the specific rule (e.g., "Presenters must not depend on repositories directly")
- **Harmonize failure**: iOS architecture convention violated — check Swift view/test naming patterns
