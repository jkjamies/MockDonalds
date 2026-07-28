package com.mockdonalds.app.features.order.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.MenuItem

class CategoryDetailStateRobot : StateRobot<CategoryDetailUiState, CategoryDetailEvent>() {

    override fun defaultState() = CategoryDetailUiState(
        categoryId = "burgers",
        categoryName = "Burgers",
        items = listOf(
            MenuItem(
                id = "1",
                title = "Big Mac",
                restaurantChain = "Sample Platter",
                imageUrl = "",
                servingSize = "214g",
                categoryId = "burgers",
            ),
        ),
        cartSummary = CartSummary(itemCount = 2, total = "\$12.99"),
        eventSink = createEventSink(),
    )

    fun stateWithNoItems() = defaultState().copy(
        items = emptyList(),
        eventSink = createEventSink(),
    )

    fun stateWithNoCart() = defaultState().copy(
        cartSummary = null,
        eventSink = createEventSink(),
    )
}
