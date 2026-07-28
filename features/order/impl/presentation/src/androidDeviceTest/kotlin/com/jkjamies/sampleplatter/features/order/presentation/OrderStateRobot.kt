package com.jkjamies.sampleplatter.features.order.presentation

import com.jkjamies.sampleplatter.core.test.StateRobot
import com.jkjamies.sampleplatter.features.order.api.domain.CartSummary
import com.jkjamies.sampleplatter.features.order.api.domain.CategoryPreview

class OrderStateRobot : StateRobot<OrderUiState, OrderEvent>() {

    override fun defaultState() = OrderUiState(
        categoryPreviews = listOf(
            CategoryPreview(id = "burgers", name = "Burgers", firstItemImageUrl = null, itemCount = 2),
            CategoryPreview(id = "drinks", name = "Drinks", firstItemImageUrl = null, itemCount = 0),
        ),
        cartSummary = CartSummary(itemCount = 2, total = "\$12.99"),
        eventSink = createEventSink(),
    )

    fun stateWithNoCart() = defaultState().copy(
        cartSummary = null,
        eventSink = createEventSink(),
    )

    fun stateWithEmptyPreviews() = defaultState().copy(
        categoryPreviews = emptyList(),
        eventSink = createEventSink(),
    )
}
