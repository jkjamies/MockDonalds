package com.mockdonalds.app.features.order.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.mockdonalds.app.features.order.test.FakeGetOrderContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

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
                    state.featuredItems.size shouldBe 1
                    state.cartSummary?.itemCount shouldBe 1
                }
            }
        }

        When("the content updates") {
            Then("the presenter should emit updated state") {
                presenterTestOf(
                    presentFunction = {
                        OrderPresenter(
                            navigator = navigator,
                            getOrderContent = fakeGetOrderContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(2)
                    fakeGetOrderContent.emit(
                        FakeGetOrderContent.DEFAULT.copy(
                            cartSummary = FakeGetOrderContent.DEFAULT.cartSummary.copy(itemCount = 3),
                        ),
                    )
                    val updated = awaitItem()
                    updated.cartSummary?.itemCount shouldBe 3
                }
            }
        }
    }
})
