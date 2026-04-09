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

## TestCenterPostDispatchers

Always use `TestCenterPostDispatchers()` (which wraps `StandardTestDispatcher`). It routes `default`, `io`, and `main` to a single test dispatcher for deterministic execution. Never use `DefaultCenterPostDispatchers` in tests.

## Presenter Test Pattern

```kotlin
class OrderPresenterTest : BehaviorSpec({

    Given("an order presenter with content available") {
        val fakeGetOrderContent = FakeGetOrderContent()
        val dispatchers = TestCenterPostDispatchers()
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
