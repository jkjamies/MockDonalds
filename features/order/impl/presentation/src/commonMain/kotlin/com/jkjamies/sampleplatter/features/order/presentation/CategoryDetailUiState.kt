package com.jkjamies.sampleplatter.features.order.presentation

import com.jkjamies.sampleplatter.features.order.api.domain.CartSummary
import com.jkjamies.sampleplatter.features.order.api.domain.MenuItem
import com.slack.circuit.runtime.CircuitUiState

data class CategoryDetailUiState(
    val categoryId: String,
    val categoryName: String,
    val items: List<MenuItem> = emptyList(),
    val cartSummary: CartSummary? = null,
    val eventSink: (CategoryDetailEvent) -> Unit,
) : CircuitUiState

sealed class CategoryDetailEvent {
    data object BackPressed : CategoryDetailEvent()
    data class AddToOrder(val itemId: String) : CategoryDetailEvent()
    data object CartClicked : CategoryDetailEvent()
}
