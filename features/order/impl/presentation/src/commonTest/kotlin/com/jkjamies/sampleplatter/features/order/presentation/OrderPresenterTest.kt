package com.jkjamies.sampleplatter.features.order.presentation

import com.jkjamies.sampleplatter.core.test.TestStrataDispatchers
import com.jkjamies.sampleplatter.features.order.api.domain.CategoryPreview
import com.jkjamies.sampleplatter.features.order.api.navigation.CategoryDetailScreen
import com.jkjamies.sampleplatter.features.order.api.navigation.OrderScreen
import com.jkjamies.sampleplatter.features.order.test.FakeGetOrderContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class OrderPresenterTest : BehaviorSpec({

    Given("an order presenter with content available") {
        val fakeGetOrderContent = FakeGetOrderContent()
        val dispatchers = TestStrataDispatchers()
        val navigator = FakeNavigator(OrderScreen)

        When("the presenter emits state") {
            Then("it should populate categoryPreviews and cart summary") {
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
                    initial.categoryPreviews shouldBe emptyList()

                    val state = awaitItem()
                    state.categoryPreviews.size shouldBe 6
                    state.categoryPreviews.first().id shouldBe "featured"
                    state.cartSummary?.itemCount shouldBe 2
                }
            }
        }

        When("CategoryTapped is fired") {
            Then("the presenter navigates to CategoryDetailScreen for the selected id") {
                presenterTestOf(
                    presentFunction = {
                        OrderPresenter(
                            navigator = navigator,
                            getOrderContent = fakeGetOrderContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(1)
                    val state = awaitItem()
                    state.eventSink(OrderEvent.CategoryTapped("burgers"))
                    val event = navigator.awaitNextScreen()
                    event shouldBe CategoryDetailScreen("burgers")
                }
            }
        }

        When("the content updates") {
            Then("the presenter emits updated state") {
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
                            categoryPreviews = listOf(
                                CategoryPreview(id = "x", name = "X", firstItemImageUrl = null, itemCount = 0),
                            ),
                        ),
                    )
                    val updated = awaitItem()
                    updated.categoryPreviews.size shouldBe 1
                    updated.categoryPreviews.first().id shouldBe "x"
                }
            }
        }
    }
})
