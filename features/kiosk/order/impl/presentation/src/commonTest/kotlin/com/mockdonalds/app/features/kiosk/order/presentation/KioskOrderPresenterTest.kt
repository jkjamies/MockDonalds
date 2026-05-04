package com.mockdonalds.app.features.kiosk.order.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.kiosk.attract.api.navigation.AttractScreen
import com.mockdonalds.app.features.kiosk.order.api.navigation.KioskOrderScreen
import com.mockdonalds.app.features.order.test.FakeGetOrderContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class KioskOrderPresenterTest : BehaviorSpec({

    Given("a kiosk order presenter consuming FakeGetOrderContent") {
        val getOrderContent = FakeGetOrderContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(KioskOrderScreen)

        When("initial state emits") {
            Then("it auto-selects the first category") {
                presenterTestOf(
                    presentFunction = {
                        KioskOrderPresenter(
                            navigator = navigator,
                            getOrderContent = getOrderContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    awaitItem() // empty
                    val state = awaitItem()
                    state.categories.size shouldBe 2
                    state.selectedCategoryId shouldBe state.categories.first().id
                }
            }
        }

        When("CategorySelected is dispatched") {
            Then("the selectedCategoryId updates and items refresh") {
                presenterTestOf(
                    presentFunction = {
                        KioskOrderPresenter(
                            navigator = navigator,
                            getOrderContent = getOrderContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(1)
                    val state = awaitItem()
                    val secondId = state.categories.last().id
                    state.eventSink(KioskOrderEvent.CategorySelected(secondId))
                    val updated = awaitItem()
                    updated.selectedCategoryId shouldBe secondId
                }
            }
        }

        When("BackPressed is dispatched") {
            Then("the navigator resets to AttractScreen") {
                presenterTestOf(
                    presentFunction = {
                        KioskOrderPresenter(
                            navigator = navigator,
                            getOrderContent = getOrderContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(1)
                    awaitItem().eventSink(KioskOrderEvent.BackPressed)
                    navigator.awaitResetRoot().newRoot shouldBe AttractScreen
                }
            }
        }

        When("CancelOrderPressed is dispatched") {
            Then("the navigator also resets to AttractScreen") {
                presenterTestOf(
                    presentFunction = {
                        KioskOrderPresenter(
                            navigator = navigator,
                            getOrderContent = getOrderContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(1)
                    awaitItem().eventSink(KioskOrderEvent.CancelOrderPressed)
                    navigator.awaitResetRoot().newRoot shouldBe AttractScreen
                }
            }
        }
    }
})
