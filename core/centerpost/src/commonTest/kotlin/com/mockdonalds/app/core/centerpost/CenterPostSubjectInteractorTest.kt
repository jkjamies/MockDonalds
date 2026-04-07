package com.mockdonalds.app.core.centerpost

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class CenterPostSubjectInteractorTest : BehaviorSpec({

    Given("a subject interactor with a backing flow") {
        val backingFlow = MutableStateFlow("initial")
        val interactor = object : CenterPostSubjectInteractor<Unit, String>() {
            override fun createObservable(params: Unit): Flow<String> = backingFlow
        }

        When("params are emitted and flow is collected") {
            Then("it should emit the current value") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem() shouldBe "initial"
                }
            }
        }

        When("the backing flow updates") {
            Then("it should emit the new value") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem() shouldBe "initial"
                    backingFlow.value = "updated"
                    awaitItem() shouldBe "updated"
                }
            }
        }
    }

    Given("a subject interactor with distinct until changed") {
        val backingFlow = MutableStateFlow("same")
        val interactor = object : CenterPostSubjectInteractor<Unit, String>() {
            override fun createObservable(params: Unit): Flow<String> = backingFlow
        }

        When("the same value is emitted again") {
            Then("it should not re-emit") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem() shouldBe "same"
                    backingFlow.value = "same"
                    expectNoEvents()
                }
            }
        }
    }
})
