package com.mockdonalds.app.features.order.domain

import com.mockdonalds.app.features.order.api.domain.GetOrderContent
import com.mockdonalds.app.features.order.api.domain.OrderContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ContributesBinding(AppScope::class)
class GetOrderContentImpl(
    private val repository: OrderRepository,
) : GetOrderContent() {
    override fun createObservable(params: Unit): Flow<OrderContent> {
        return combine(
            repository.getMenuCategories(),
            repository.getFeaturedItems(),
            repository.getCartSummary(),
        ) { categories, items, cart ->
            OrderContent(
                categories = categories,
                featuredItems = items,
                cartSummary = cart,
            )
        }
    }
}
