package com.jkjamies.sampleplatter.features.order.test

import com.jkjamies.sampleplatter.features.order.api.domain.CartSummary
import com.jkjamies.sampleplatter.features.order.api.domain.CategoryPreview
import com.jkjamies.sampleplatter.features.order.api.domain.GetOrderContent
import com.jkjamies.sampleplatter.features.order.api.domain.OrderContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
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
            categoryPreviews = listOf(
                CategoryPreview(id = "featured", name = "Featured", firstItemImageUrl = null, itemCount = 0),
                CategoryPreview(id = "burgers", name = "Burgers", firstItemImageUrl = null, itemCount = 0),
                CategoryPreview(id = "chicken", name = "Chicken", firstItemImageUrl = null, itemCount = 0),
                CategoryPreview(id = "sides", name = "Sides", firstItemImageUrl = null, itemCount = 0),
                CategoryPreview(id = "drinks", name = "Drinks", firstItemImageUrl = null, itemCount = 0),
                CategoryPreview(id = "desserts", name = "Desserts", firstItemImageUrl = null, itemCount = 0),
            ),
            cartSummary = CartSummary(itemCount = 2, total = "\$36.00"),
        )
    }
}
