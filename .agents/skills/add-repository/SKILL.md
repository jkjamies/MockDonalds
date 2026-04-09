---
name: add-repository
description: Add a new repository with interface, implementation, and test. Use when adding a new data source to a feature.
---

# Add Repository

Create a repository interface, implementation, and test.

**Parameters**: feature name, repository name

## Reference Standards

- DI patterns: `.agents/standards/dependency-injection.md`

## Reference

- Interface: `features/order/impl/domain/src/commonMain/.../OrderRepository.kt`
- Impl: `features/order/impl/data/src/commonMain/.../OrderRepositoryImpl.kt`
- Test: `features/order/impl/data/src/commonTest/.../OrderRepositoryImplTest.kt`

## Files to Create

### 1. Interface — `impl/domain/`

`features/{feature}/impl/domain/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/domain/{Name}Repository.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.domain

import kotlinx.coroutines.flow.Flow

interface {Name}Repository {
    fun getData(): Flow<{DataType}>
}
```

Repository functions should return `Flow<T>` for streaming data. Use `suspend fun` only for one-shot operations (rare).

### 2. Implementation — `impl/data/`

`features/{feature}/impl/data/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/data/{Name}RepositoryImpl.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data

import com.mockdonalds.app.features.{feature}.domain.{Name}Repository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class {Name}RepositoryImpl : {Name}Repository {
    override fun getData(): Flow<{DataType}> = flowOf(
        // implementation
    )
}
```

### 3. Test — `impl/data/commonTest/`

`features/{feature}/impl/data/src/commonTest/kotlin/com/mockdonalds/app/features/{feature}/data/{Name}RepositoryImplTest.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class {Name}RepositoryImplTest : BehaviorSpec({

    Given("a {Name}RepositoryImpl") {
        val repository = {Name}RepositoryImpl()

        When("fetching data") {
            Then("it should return expected values") {
                val result = repository.getData().first()
                // assertions
            }
        }
    }
})
```

## Key Rules

- Interface in `impl/domain/` — visible to use cases in the same feature
- Impl in `impl/data/` — private, must have `@ContributesBinding(AppScope::class)`
- Impl name must end with `RepositoryImpl` (Konsist enforces this)
- Impl must implement the interface (Konsist enforces this)
- Presenters must NOT depend on repositories directly — only through use cases

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes. It will:

- Detect which modules were affected by the new repository files
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch naming violations, missing `@ContributesBinding`, layer isolation issues

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
