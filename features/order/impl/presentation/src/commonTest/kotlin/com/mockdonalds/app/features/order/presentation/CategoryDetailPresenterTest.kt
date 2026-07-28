package com.mockdonalds.app.features.order.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.order.api.domain.CategoryDetailContent
import com.mockdonalds.app.features.order.api.domain.MenuItem
import com.mockdonalds.app.features.order.api.navigation.CategoryDetailScreen
import com.mockdonalds.app.features.order.test.FakeGetCategoryDetailContent
import com.mockdonalds.app.features.order.test.FakeGetOrderContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CategoryDetailPresenterTest : BehaviorSpec({

    Given("a CategoryDetailPresenter for the burgers screen") {
        val screen = CategoryDetailScreen(categoryId = "burgers")
        val getDetail = FakeGetCategoryDetailContent(
            initial = CategoryDetailContent(
                categoryId = "burgers",
                categoryName = "Burgers",
                items = listOf(
                    MenuItem(
                        id = "1",
                        title = "Big Mac",
                        restaurantChain = "Sample Platter",
                        imageUrl = "img",
                        servingSize = "214g",
                        categoryId = "burgers",
                    ),
                ),
            ),
        )
        val getOrder = FakeGetOrderContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(screen)

        When("the presenter emits state") {
            Then("it exposes the category name and items") {
                presenterTestOf(
                    presentFunction = {
                        CategoryDetailPresenter(
                            screen = screen,
                            navigator = navigator,
                            getCategoryDetailContent = getDetail,
                            getOrderContent = getOrder,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.categoryId shouldBe "burgers"
                    val populated = expectMostRecentItem()
                    populated.categoryName shouldBe "Burgers"
                    populated.items.size shouldBe 1
                    populated.items.first().title shouldBe "Big Mac"
                }
            }
        }

        When("BackPressed is fired") {
            Then("the presenter pops the navigator") {
                presenterTestOf(
                    presentFunction = {
                        CategoryDetailPresenter(
                            screen = screen,
                            navigator = navigator,
                            getCategoryDetailContent = getDetail,
                            getOrderContent = getOrder,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = expectMostRecentItem()
                    state.eventSink(CategoryDetailEvent.BackPressed)
                    navigator.awaitPop()
                }
            }
        }
    }
})
