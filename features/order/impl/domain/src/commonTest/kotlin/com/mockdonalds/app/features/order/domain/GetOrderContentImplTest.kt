package com.mockdonalds.app.features.order.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.FeaturedItem
import com.mockdonalds.app.features.order.api.domain.MenuCategory
import com.mockdonalds.app.features.order.api.domain.OrderContent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetOrderContentImplTest : BehaviorSpec({

    Given("an order content interactor with repository data") {
        val categories = MutableStateFlow(
            listOf(MenuCategory(id = "1", name = "Burgers")),
        )
        val featuredItems = MutableStateFlow(
            listOf(
                FeaturedItem(
                    id = "1",
                    title = "Test Burger",
                    price = "$10",
                    description = "Desc",
                    imageUrl = "",
                    tag = "NEW",
                    isPrimary = true,
                ),
            ),
        )
        val cartSummary = MutableStateFlow(
            CartSummary(itemCount = 1, total = "$10.00"),
        )

        val repository = object : OrderRepository {
            override fun getMenuCategories(): Flow<List<MenuCategory>> = categories
            override fun getFeaturedItems(): Flow<List<FeaturedItem>> = featuredItems
            override fun getCartSummary(): Flow<CartSummary> = cartSummary
        }

        val interactor = GetOrderContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it should combine all repository data into OrderContent") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content shouldBe OrderContent(
                        categories = categories.value,
                        featuredItems = featuredItems.value,
                        cartSummary = cartSummary.value,
                    )
                }
            }
        }

        When("the repository categories update") {
            Then("it should emit updated content") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem()
                    categories.value = listOf(
                        MenuCategory(id = "1", name = "Burgers"),
                        MenuCategory(id = "2", name = "Fries"),
                    )
                    val updated = awaitItem()
                    updated.categories.size shouldBe 2
                }
            }
        }
    }
})
