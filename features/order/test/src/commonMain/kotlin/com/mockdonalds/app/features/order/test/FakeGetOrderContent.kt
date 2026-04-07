package com.mockdonalds.app.features.order.test

import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.FeaturedItem
import com.mockdonalds.app.features.order.api.domain.GetOrderContent
import com.mockdonalds.app.features.order.api.domain.MenuCategory
import com.mockdonalds.app.features.order.api.domain.OrderContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetOrderContent(
    initial: OrderContent = DEFAULT,
) : GetOrderContent() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<OrderContent> = _content

    fun emit(content: OrderContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = OrderContent(
            categories = listOf(
                MenuCategory(id = "1", name = "Burgers"),
                MenuCategory(id = "2", name = "Fries"),
            ),
            featuredItems = listOf(
                FeaturedItem(
                    id = "1",
                    title = "Test Burger",
                    price = "$10",
                    description = "A test burger",
                    imageUrl = "",
                    tag = "NEW",
                    isPrimary = true,
                ),
            ),
            cartSummary = CartSummary(itemCount = 1, total = "$10.00"),
        )
    }
}
