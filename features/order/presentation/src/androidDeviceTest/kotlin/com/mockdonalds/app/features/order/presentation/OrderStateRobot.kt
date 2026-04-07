package com.mockdonalds.app.features.order.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.FeaturedItem
import com.mockdonalds.app.features.order.api.domain.MenuCategory

class OrderStateRobot : StateRobot<OrderUiState, OrderEvent>() {

    override fun defaultState() = OrderUiState(
        categories = listOf(
            MenuCategory(id = "1", name = "Burgers"),
            MenuCategory(id = "2", name = "Sides"),
        ),
        selectedCategoryId = "1",
        featuredItems = listOf(
            FeaturedItem(
                id = "1",
                title = "Big Mac",
                price = "$5.99",
                description = "Two patties, special sauce",
                imageUrl = "",
                tag = "POPULAR",
                isPrimary = true,
            ),
        ),
        cartSummary = CartSummary(itemCount = 2, total = "$12.99"),
        eventSink = createEventSink(),
    )

    fun stateWithNoCart() = defaultState().copy(
        cartSummary = null,
        eventSink = createEventSink(),
    )

    fun stateWithEmptyMenu() = defaultState().copy(
        featuredItems = emptyList(),
        eventSink = createEventSink(),
    )
}
