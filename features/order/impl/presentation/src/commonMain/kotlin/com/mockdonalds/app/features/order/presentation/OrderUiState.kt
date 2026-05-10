package com.mockdonalds.app.features.order.presentation

import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.CategoryPreview
import com.slack.circuit.runtime.CircuitUiState

data class OrderUiState(
    val categoryPreviews: List<CategoryPreview> = emptyList(),
    val cartSummary: CartSummary? = null,
    val eventSink: (OrderEvent) -> Unit,
) : CircuitUiState

sealed class OrderEvent {
    data class CategoryTapped(val id: String) : OrderEvent()
    data object CartClicked : OrderEvent()
}
