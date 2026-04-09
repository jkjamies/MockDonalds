---
name: add-unit-tests
description: Identify and fill unit test gaps for changed code by analyzing the branch diff. Creates test files following Kotest BehaviorSpec patterns with fakes.
---

# Add Unit Tests

Identify changed/new production classes and create or update their unit tests.

## Reference Standards

- Testing conventions, quality rules, and tech stack: `.agents/standards/testing.md`

## Scope

This skill covers Kotest `BehaviorSpec` unit tests in `commonTest/`. It does NOT cover:
- Android UI tests (Robot pattern in `androidDeviceTest/` — use `add-ui-tests`)
- Navigation and integration tests in `navint-tests/` (JUnit4, require emulator — use `add-tests` which handles navint-tests awareness)

## Steps

### 1. Identify Test Gaps

```bash
git diff origin/main...HEAD --name-only -- '*.kt'
```

For each changed file, determine if a test is needed:

| Source File | Expected Test |
|------------|---------------|
| `features/{name}/impl/domain/*Impl.kt` | `features/{name}/impl/domain/src/commonTest/.../...ImplTest.kt` |
| `features/{name}/impl/data/*RepositoryImpl.kt` | `features/{name}/impl/data/src/commonTest/.../...RepositoryImplTest.kt` |
| `features/{name}/impl/presentation/*Presenter.kt` | `features/{name}/impl/presentation/src/commonTest/.../...PresenterTest.kt` |

Check if the test file exists. If it exists, check if new code paths need additional test cases.

### 2. Create Missing Tests

#### Presenter Test Template

Reference: `features/order/impl/presentation/src/commonTest/.../OrderPresenterTest.kt`

```kotlin
package com.mockdonalds.app.features.{name}.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.{name}.api.navigation.{Feature}Screen
import com.mockdonalds.app.features.{name}.test.FakeGet{Feature}Content
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class {Feature}PresenterTest : BehaviorSpec({

    Given("a {name} presenter with content available") {
        val fakeContent = FakeGet{Feature}Content()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator({Feature}Screen)

        When("the presenter emits state") {
            Then("it should start with empty defaults then populate") {
                presenterTestOf(
                    presentFunction = {
                        {Feature}Presenter(
                            navigator = navigator,
                            get{Feature}Content = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    // assert initial empty state

                    val state = awaitItem()
                    // assert populated state
                }
            }
        }
    }
})
```

#### Use Case Impl Test Template

Reference: `features/order/impl/domain/src/commonTest/.../GetOrderContentImplTest.kt`

```kotlin
package com.mockdonalds.app.features.{name}.domain

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class Get{Feature}ContentImplTest : BehaviorSpec({

    Given("a Get{Feature}ContentImpl") {
        val repository = // create fake or test instance
        val impl = Get{Feature}ContentImpl(repository)

        When("observing content") {
            Then("it should combine repository flows correctly") {
                val result = impl.createObservable(Unit).first()
                // assert combined result
            }
        }
    }
})
```

#### Repository Impl Test Template

Reference: `features/order/impl/data/src/commonTest/.../OrderRepositoryImplTest.kt`

```kotlin
package com.mockdonalds.app.features.{name}.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class {Feature}RepositoryImplTest : BehaviorSpec({

    Given("a {Feature}RepositoryImpl") {
        val repository = {Feature}RepositoryImpl()

        When("fetching data") {
            Then("it should return expected values") {
                val result = repository.getData().first()
                result shouldBe expectedValue
            }
        }
    }
})
```

### 3. Ensure Fakes Exist

If the test requires a fake that doesn't exist yet, create it in `features/{name}/test/src/commonMain/`:

Reference: `features/order/test/src/commonMain/.../FakeGetOrderContent.kt`

```kotlin
@ContributesBinding(AppScope::class)
class Fake{Name} @Inject constructor(
    initial: {ContentType} = DEFAULT,
) : {AbstractClass}() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<{ContentType}> = _content

    fun emit(content: {ContentType}) {
        _content.value = content
    }

    companion object {
        val DEFAULT = // default test data
    }
}
```

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes. It will:

- Detect which modules have new or changed test files
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch test naming violations, placement issues, and convention problems

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
