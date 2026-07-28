package com.jkjamies.sampleplatter.features.order.domain

import app.cash.turbine.test
import com.jkjamies.sampleplatter.features.order.api.domain.CartSummary
import com.jkjamies.sampleplatter.features.order.api.domain.CategoryPreview
import com.jkjamies.sampleplatter.features.order.api.domain.MenuItem
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class GetCategoryDetailContentImplTest : BehaviorSpec({

    Given("a repository that knows the burgers category and exposes its items") {
        val items = MutableStateFlow(
            listOf(
                MenuItem(
                    id = "1",
                    title = "Signature Stack",
                    restaurantChain = "Sample Platter",
                    imageUrl = "img",
                    servingSize = "214g",
                    categoryId = "burgers",
                ),
            ),
        )

        val repository = object : OrderRepository {
            override fun getCategoryPreviews(): Flow<List<CategoryPreview>> = flowOf(emptyList())
            override fun getMenuItemsByCategory(categoryId: String): Flow<List<MenuItem>> = items
            override fun categoryName(categoryId: String): String? =
                if (categoryId == "burgers") "Burgers" else null
            override fun getCartSummary(): Flow<CartSummary> = flowOf(CartSummary(0, "\$0"))
        }

        val interactor = GetCategoryDetailContentImpl(repository)

        When("the interactor is invoked with a known categoryId") {
            Then("it emits CategoryDetailContent with the resolved name and items") {
                interactor("burgers")
                interactor.flow.test {
                    val detail = awaitItem()
                    detail.categoryId shouldBe "burgers"
                    detail.categoryName shouldBe "Burgers"
                    detail.items.size shouldBe 1
                    detail.items.first().title shouldBe "Signature Stack"
                }
            }
        }

        When("the interactor is invoked with an unknown categoryId") {
            Then("the categoryName falls back to empty string") {
                interactor("unknown")
                interactor.flow.test {
                    val detail = awaitItem()
                    detail.categoryName shouldBe ""
                }
            }
        }
    }
})
