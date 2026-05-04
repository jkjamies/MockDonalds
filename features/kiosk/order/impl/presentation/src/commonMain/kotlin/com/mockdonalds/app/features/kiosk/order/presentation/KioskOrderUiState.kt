package com.mockdonalds.app.features.kiosk.order.presentation

import com.mockdonalds.app.features.shared.menu.api.domain.CartSummary
import com.mockdonalds.app.features.shared.menu.api.domain.MenuCategory
import com.mockdonalds.app.features.shared.menu.api.domain.MenuItem
import com.slack.circuit.runtime.CircuitUiState

data class KioskOrderUiState(
    val categories: List<MenuCategory>,
    val selectedCategoryId: String?,
    val itemsForSelectedCategory: List<MenuItem>,
    val cartSummary: CartSummary?,
    val eventSink: (KioskOrderEvent) -> Unit,
) : CircuitUiState {
    val selectedCategoryName: String?
        get() = categories.firstOrNull { it.id == selectedCategoryId }?.name
}
