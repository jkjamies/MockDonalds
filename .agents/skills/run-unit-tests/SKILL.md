---
name: run-unit-tests
description: Run the Kotest unit test suite. Use to validate logic changes in domain, data, or presentation layers.
---

# Run Unit Tests

## When to Use

After modifying any Kotlin source files in `impl/domain`, `impl/data`, or `impl/presentation` modules.

## Run All Unit Tests

```bash
./gradlew testAndroidHostTest
```

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
