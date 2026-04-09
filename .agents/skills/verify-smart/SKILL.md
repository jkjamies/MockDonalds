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

### 3. Unit Tests

Scope unit tests to affected modules:
```bash
# If features/home/ changed:
./gradlew :features:home:impl:domain:testAndroidHostTest
./gradlew :features:home:impl:data:testAndroidHostTest
./gradlew :features:home:impl:presentation:testAndroidHostTest

# If core/centerpost/ changed:
./gradlew :core:centerpost:testAndroidHostTest
```

### 4. Architecture Tests

Always run — these are fast and catch structural issues regardless of what changed:

```bash
./gradlew :architecture-check:test
swift test --package-path iosApp/ArchitectureCheck
```

### 5. Build (if build files changed)

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

## When to Use

- After targeted changes to 1-2 features or modules
- During iterative development when full verify is too slow
- For documentation-only changes (architecture tests catch stale references)

For comprehensive validation (pre-merge, post-scaffolding), use `verify` instead.
