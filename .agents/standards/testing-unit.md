# Unit Testing Standards

Unit tests verify individual classes in isolation: use case implementations, repository implementations, and presenters. Each class is tested independently with fakes replacing all dependencies.

> Shared conventions (test stack, quality standards, fakes, infrastructure) are in [testing.md](testing.md).

## Scope

| What's tested | What's real | What's faked |
|---------------|-------------|--------------|
| Single class (UseCase, Repository, Presenter) | The class under test | All dependencies (use cases, repositories, auth) |

## Run Commands

```bash
# All Kotlin unit tests
./gradlew testAndroidHostTest

# Single module
./gradlew :features:{name}:impl:domain:testAndroidHostTest
./gradlew :features:{name}:impl:data:testAndroidHostTest
./gradlew :features:{name}:impl:presentation:testAndroidHostTest
```

## Spec Style

All tests use `BehaviorSpec` (Given/When/Then). No `FunSpec`, `StringSpec`, or other Kotest styles.
Enforced by: `TestFileNamingTest` -- checks all specs in `commonTest` extend `BehaviorSpec`.

## File Placement

| Source | Test Location |
|--------|---------------|
| `features/{name}/impl/domain/*Impl.kt` | `features/{name}/impl/domain/src/commonTest/.../...ImplTest.kt` |
| `features/{name}/impl/data/*RepositoryImpl.kt` | `features/{name}/impl/data/src/commonTest/.../...RepositoryImplTest.kt` |
| `features/{name}/impl/presentation/*Presenter.kt` | `features/{name}/impl/presentation/src/commonTest/.../...PresenterTest.kt` |

Enforced by: `TestModuleCoverageTest` -- every Impl, Presenter, and RepositoryImpl has a test file.

## Coverage Requirements

Every `UseCaseImpl`, every `RepositoryImpl`, and every `Presenter` must have a corresponding test. No exceptions.

## TestStrataDispatchers

Always use `TestStrataDispatchers()` (which wraps `StandardTestDispatcher`). It routes `default`, `io`, and `main` to a single test dispatcher for deterministic execution. Never use `DefaultStrataDispatchers` in tests, and never construct a `StandardTestDispatcher` by hand — going through the fixture is what keeps its scheduler reachable.

### Advance the scheduler when the subject dispatches

A `StandardTestDispatcher` queues onto a `TestCoroutineScheduler` that only drains when advanced. `runTest` normally does that and is banned here — Kotest supplies the coroutine context instead — so the test advances it:

```kotlin
repository.getMenuItemsByCategory("burgers").test {
    fixture.advanceUntilIdle()          // let the queued refresh run
    awaitItem() shouldContain cachedItem
    fixture.advanceUntilIdle()          // leave nothing parked
    cancel()
}
```

Advance before awaiting anything the dispatched work produces, and again before cancelling. Skipping the second one is not merely untidy: cancelling a coroutine parked on an unadvanced scheduler can never complete, because the cancellation itself has to be delivered through that scheduler. The failure mode is an unkillable hang — `withTimeout` does not fire, Turbine's `awaitItem` timeout does not fire, and the Gradle test task runs until something kills it from outside.

Most suites never need this; they only pass the fixture to a subject that stores it. It matters wherever production code actually schedules on the injected dispatchers, such as `withContext(dispatchers.io)`.

## Presenter Test Pattern

```kotlin
class OrderPresenterTest : BehaviorSpec({

    Given("an order presenter with content available") {
        val fakeGetOrderContent = FakeGetOrderContent()
        val dispatchers = TestStrataDispatchers()
        val navigator = FakeNavigator(OrderScreen)

        When("the presenter emits state") {
            Then("it should start with empty defaults then populate") {
                presenterTestOf(
                    presentFunction = {
                        OrderPresenter(
                            navigator = navigator,
                            getOrderContent = fakeGetOrderContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.categories shouldBe emptyList()

                    val state = awaitItem()
                    state.categories.size shouldBe 2
                }
            }
        }

        When("the content updates") {
            Then("the presenter should emit updated state") {
                presenterTestOf(
                    presentFunction = { OrderPresenter(navigator, fakeGetOrderContent, dispatchers) },
                ) {
                    skipItems(2)
                    fakeGetOrderContent.emit(
                        FakeGetOrderContent.DEFAULT.copy(cartSummary = /*...*/)
                    )
                    val updated = awaitItem()
                    updated.cartSummary?.itemCount shouldBe 3
                }
            }
        }
    }
})
```

## Use Case Test Pattern

```kotlin
class GetHomeContentImplTest : BehaviorSpec({

    Given("a GetHomeContentImpl") {
        val repository = FakeHomeRepository()
        val impl = GetHomeContentImpl(repository)

        When("observing content") {
            Then("it should combine repository flows correctly") {
                val result = impl.createObservable(Unit).first()
                result.userName shouldBe expectedUserName
            }
        }
    }
})
```

## Repository Test Pattern

```kotlin
class OrderRepositoryImplTest : BehaviorSpec({

    Given("an OrderRepositoryImpl") {
        val repository = OrderRepositoryImpl()

        When("fetching data") {
            Then("it should return expected values") {
                val result = repository.getData().first()
                result shouldBe expectedValue
            }
        }
    }
})
```

## Enforcement

- `TestFileNamingTest` -- all specs extend BehaviorSpec, no runBlocking/runTest/UnconfinedTestDispatcher
- `TestModuleCoverageTest` -- every Impl, Presenter, RepositoryImpl has a test
- `TestDoubleConventionsTest` -- fakes in test/commonMain, no mockk, every use case has a Fake
