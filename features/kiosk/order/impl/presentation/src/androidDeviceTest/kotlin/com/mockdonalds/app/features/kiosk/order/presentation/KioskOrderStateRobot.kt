package com.mockdonalds.app.features.kiosk.order.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.shared.menu.api.domain.CartSummary
import com.mockdonalds.app.features.shared.menu.api.domain.MenuCategory
import com.mockdonalds.app.features.shared.menu.api.domain.MenuItem

class KioskOrderStateRobot : StateRobot<KioskOrderUiState, KioskOrderEvent>() {

    private val sampleCategories = listOf(
        MenuCategory(id = "burgers", name = "Burgers"),
        MenuCategory(id = "fries-sides", name = "Fries & Sides"),
        MenuCategory(id = "beverages", name = "Beverages"),
    )

    private val sampleItems = listOf(
        MenuItem(
            id = "burger-bigmac",
            categoryId = "burgers",
            name = "Big Mac",
            priceFormatted = "$4.59",
            calories = 540,
            imageUrl = "https://example.test/menu/big-mac.png",
        ),
        MenuItem(
            id = "burger-mcdouble",
            categoryId = "burgers",
            name = "McDouble",
            priceFormatted = "$1.99",
            calories = 380,
            imageUrl = "https://example.test/menu/mcdouble.png",
        ),
    )

    override fun defaultState() = KioskOrderUiState(
        categories = sampleCategories,
        selectedCategoryId = "burgers",
        itemsForSelectedCategory = sampleItems,
        cartSummary = CartSummary(itemCount = 0, total = "$0.00"),
        eventSink = createEventSink(),
    )

    fun emptyCartState() = defaultState().copy(
        cartSummary = CartSummary(itemCount = 0, total = "$0.00"),
    )

    fun cartWithItemsState() = defaultState().copy(
        cartSummary = CartSummary(itemCount = 2, total = "$6.58"),
    )
}
