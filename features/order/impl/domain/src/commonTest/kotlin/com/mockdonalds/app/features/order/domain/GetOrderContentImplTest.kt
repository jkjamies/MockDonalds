package com.mockdonalds.app.features.order.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.CategoryPreview
import com.mockdonalds.app.features.order.api.domain.MenuItem
import com.mockdonalds.app.features.order.api.domain.OrderContent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetOrderContentImplTest : BehaviorSpec({

    Given("a repository with category previews and a cart summary") {
        val previews = MutableStateFlow(
            listOf(
                CategoryPreview(id = "burgers", name = "Burgers", firstItemImageUrl = null, itemCount = 0),
            ),
        )
        val cart = MutableStateFlow(CartSummary(itemCount = 1, total = "\$10.00"))

        val repository = object : OrderRepository {
            override fun getCategoryPreviews(): Flow<List<CategoryPreview>> = previews
            override fun getMenuItemsByCategory(categoryId: String): Flow<List<MenuItem>> =
                MutableStateFlow(emptyList())
            override fun categoryName(categoryId: String): String? = null
            override fun getCartSummary(): Flow<CartSummary> = cart
        }

        val interactor = GetOrderContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it combines category previews and the cart summary into OrderContent") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content shouldBe OrderContent(
                        categoryPreviews = previews.value,
                        cartSummary = cart.value,
                    )
                }
            }
        }

        When("the previews update") {
            Then("the interactor emits updated content") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem()
                    previews.value = listOf(
                        CategoryPreview(id = "burgers", name = "Burgers", firstItemImageUrl = null, itemCount = 0),
                        CategoryPreview(id = "drinks", name = "Drinks", firstItemImageUrl = "url", itemCount = 5),
                    )
                    val updated = awaitItem()
                    updated.categoryPreviews.size shouldBe 2
                }
            }
        }
    }
})
