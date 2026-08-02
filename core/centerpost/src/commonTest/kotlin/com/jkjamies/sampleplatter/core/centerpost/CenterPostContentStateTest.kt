package com.jkjamies.sampleplatter.core.centerpost

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class CenterPostContentStateTest : BehaviorSpec({

    Given("an interactor whose observable emits values") {
        val backing = MutableStateFlow("first")
        val interactor = object : CenterPostSubjectInteractor<Unit, String>() {
            override fun createObservable(params: Unit): Flow<String> = backing
        }

        When("contentState is collected") {
            Then("it emits Loading before the first value") {
                interactor(Unit)
                interactor.contentState.test {
                    awaitItem() shouldBe CenterPostContentState.Loading
                    awaitItem() shouldBe CenterPostContentState.Content("first")
                }
            }

            Then("subsequent values arrive as Content") {
                interactor(Unit)
                interactor.contentState.test {
                    awaitItem() shouldBe CenterPostContentState.Loading
                    awaitItem() shouldBe CenterPostContentState.Content("first")
                    backing.value = "second"
                    awaitItem() shouldBe CenterPostContentState.Content("second")
                }
            }
        }
    }

    Given("an interactor whose observable throws") {
        val failure = IllegalStateException("backend exploded")
        val interactor = object : CenterPostSubjectInteractor<Unit, String>() {
            override fun createObservable(params: Unit): Flow<String> = flow { throw failure }
        }

        When("contentState is collected") {
            Then("the failure surfaces as an Error state rather than propagating") {
                interactor(Unit)
                interactor.contentState.test {
                    awaitItem() shouldBe CenterPostContentState.Loading
                    // Uses the return value rather than relying on the smart-cast contract.
                    val error = awaitItem().shouldBeInstanceOf<CenterPostContentState.Error>()
                    error.error.shouldBeInstanceOf<CenterPostExecutionException>()
                    error.error.cause shouldBe failure
                    // contentState never completes — paramState is an infinite SharedFlow,
                    // and the catch is inside flatMapLatest so only the inner flow ends.
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the raw flow is collected instead") {
            Then("the exception still propagates — that is why contentState exists") {
                interactor(Unit)
                interactor.flow.test {
                    awaitError() shouldBe failure
                }
            }
        }
    }

    Given("an interactor whose observable emits then throws") {
        val failure = IllegalStateException("stream died mid-flight")
        val interactor = object : CenterPostSubjectInteractor<Unit, String>() {
            override fun createObservable(params: Unit): Flow<String> = flow {
                emit("partial")
                throw failure
            }
        }

        When("contentState is collected") {
            Then("the emitted value is kept and the failure follows it") {
                interactor(Unit)
                interactor.contentState.test {
                    awaitItem() shouldBe CenterPostContentState.Loading
                    awaitItem() shouldBe CenterPostContentState.Content("partial")
                    awaitItem().shouldBeInstanceOf<CenterPostContentState.Error>()
                    // contentState never completes — paramState is an infinite SharedFlow,
                    // and the catch is inside flatMapLatest so only the inner flow ends.
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    Given("a CenterPostException thrown by the observable") {
        val original = CenterPostTimeoutException(kotlin.time.Duration.ZERO, IllegalStateException("t"))
        val interactor = object : CenterPostSubjectInteractor<Unit, String>() {
            override fun createObservable(params: Unit): Flow<String> = flow { throw original }
        }

        When("contentState is collected") {
            Then("it is surfaced as-is rather than being wrapped again") {
                interactor(Unit)
                interactor.contentState.test {
                    awaitItem() shouldBe CenterPostContentState.Loading
                    awaitItem() shouldBe CenterPostContentState.Error(original)
                    // contentState never completes — paramState is an infinite SharedFlow,
                    // and the catch is inside flatMapLatest so only the inner flow ends.
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    Given("an interactor that fails once and then succeeds") {
        // The Error state is only useful if something can clear it. `invoke(Unit)` cannot:
        // params are deduped, and a Unit-param interactor has no other params to pass.
        var attempts = 0
        val interactor = object : CenterPostSubjectInteractor<Unit, String>() {
            override fun createObservable(params: Unit): Flow<String> = flow {
                attempts += 1
                if (attempts == 1) throw IllegalStateException("first attempt fails")
                emit("recovered")
            }
        }

        When("retry is called after the failure") {
            Then("the stream restarts and reaches Content") {
                interactor(Unit)
                interactor.contentState.test {
                    awaitItem() shouldBe CenterPostContentState.Loading
                    awaitItem().shouldBeInstanceOf<CenterPostContentState.Error>()

                    // Re-invoking with identical params is deduped, so this must not restart
                    // anything — the `attempts` assertion below is what proves it.
                    interactor(Unit)

                    interactor.retry()
                    awaitItem() shouldBe CenterPostContentState.Loading
                    awaitItem() shouldBe CenterPostContentState.Content("recovered")
                    cancelAndIgnoreRemainingEvents()
                }
                attempts shouldBe 2
            }
        }
    }

    Given("content state accessors") {
        When("inspecting each state") {
            Then("dataOrNull, isLoading and errorOrNull report the right case") {
                val loading: CenterPostContentState<String> = CenterPostContentState.Loading
                val content: CenterPostContentState<String> = CenterPostContentState.Content("v")
                val error: CenterPostContentState<String> =
                    CenterPostContentState.Error(CenterPostExecutionException(IllegalStateException("e")))

                loading.isLoading shouldBe true
                loading.dataOrNull shouldBe null
                loading.errorOrNull shouldBe null

                content.isLoading shouldBe false
                content.dataOrNull shouldBe "v"
                content.errorOrNull shouldBe null

                error.isLoading shouldBe false
                error.dataOrNull shouldBe null
                error.errorOrNull.shouldBeInstanceOf<CenterPostExecutionException>()
            }
        }
    }
})
