package com.mockdonalds.app.features.mobile.order.presentation

import com.mockdonalds.app.features.shared.menu.api.domain.CartSummary
import com.mockdonalds.app.features.shared.menu.api.domain.FeaturedItem
import com.mockdonalds.app.features.shared.menu.api.domain.MenuCategory
import com.slack.circuit.runtime.CircuitUiState

data class OrderUiState(
    val categories: List<MenuCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val featuredItems: List<FeaturedItem> = emptyList(),
    val cartSummary: CartSummary? = null,
    val eventSink: (OrderEvent) -> Unit,
) : CircuitUiState

sealed class OrderEvent {
    data class CategorySelected(val id: String) : OrderEvent()
    data class AddToOrder(val itemId: String) : OrderEvent()
    data object CartClicked : OrderEvent()
}
