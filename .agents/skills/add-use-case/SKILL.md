---
name: add-use-case
description: Add a new use case interactor with abstraction, implementation, fake, and test. Use when adding new business logic to a feature.
---

# Add Use Case

Create a new interactor with all four required files.

**Parameters**: feature name, use case name, type (`CenterPostInteractor` for one-shot, `CenterPostSubjectInteractor` for streaming)

## Reference Standards

- DI patterns: `.agents/standards/dependency-injection.md`
- CenterPost interactors: `.agents/standards/centerpost.md`

## Reference

- Abstract: `features/order/api/domain/src/commonMain/.../GetOrderContent.kt`
- Impl: `features/order/impl/domain/src/commonMain/.../GetOrderContentImpl.kt`
- Fake: `features/order/test/src/commonMain/.../FakeGetOrderContent.kt`
- Test: `features/order/impl/domain/src/commonTest/.../GetOrderContentImplTest.kt`

## Files to Create

### 1. Abstract Use Case — `api/domain/`

`features/{feature}/api/domain/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/api/domain/{Name}.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.api.domain

import com.mockdonalds.app.core.centerpost.CenterPostSubjectInteractor

abstract class {Name} : CenterPostSubjectInteractor<Unit, {ResultType}>()
```

For one-shot use cases, use `CenterPostInteractor` instead:
```kotlin
abstract class {Name} : CenterPostInteractor<{Params}, {ResultType}>()
```

### 2. Implementation — `impl/domain/`

`features/{feature}/impl/domain/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/domain/{Name}Impl.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.domain

import com.mockdonalds.app.features.{feature}.api.domain.{Name}
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class {Name}Impl(
    private val repository: {Feature}Repository,
) : {Name}() {
    override fun createObservable(params: Unit): Flow<{ResultType}> {
        return repository.getData()
    }
}
```

### 3. Fake — `test/`

`features/{feature}/test/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/test/Fake{Name}.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.test

import com.mockdonalds.app.features.{feature}.api.domain.{Name}
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class Fake{Name}(
    initial: {ResultType} = DEFAULT,
) : {Name}() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<{ResultType}> = _content

    fun emit(content: {ResultType}) {
        _content.value = content
    }

    companion object {
        val DEFAULT = // test default data
    }
}
```

### 4. Test — `impl/domain/commonTest/`

`features/{feature}/impl/domain/src/commonTest/kotlin/com/mockdonalds/app/features/{feature}/domain/{Name}ImplTest.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class {Name}ImplTest : BehaviorSpec({

    Given("a {Name}Impl") {
        val repository = // test repository instance
        val impl = {Name}Impl(repository)

        When("observing data") {
            Then("it should return expected content") {
                val result = impl.createObservable(Unit).first()
                // assertions
            }
        }
    }
})
```

## Key Rules

- Abstract use case in `api/domain/` — public contract
- Impl in `impl/domain/` — private, must have `@ContributesBinding(AppScope::class)`
- Fake in `test/src/commonMain/` — NOT in commonTest
- Impl must extend the abstract class (Konsist enforces this)
- Every abstract use case must have a matching Impl (Konsist enforces this)

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes. It will:

- Detect which modules were affected by the new use case files
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch naming violations, missing `@ContributesBinding`, abstract/impl pairing issues

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
