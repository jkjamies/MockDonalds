package com.mockdonalds.app.features.kiosk.order.test

import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.MenuCategory
import com.mockdonalds.app.features.order.api.domain.MenuItem
import com.mockdonalds.app.features.order.api.domain.OrderContent

/**
 * Fixture builders for KioskOrderUiState and the underlying OrderContent.
 *
 * KioskOrderPresenter consumes `GetOrderContent` from `features/order/api/domain` —
 * fakes for that interactor live in `features/order/test/`. This file provides
 * convenience defaults specifically for kiosk-order tests.
 */
object FakeKioskOrderContent {
    val DEFAULT_CATEGORIES: List<MenuCategory> = listOf(
        MenuCategory(id = "burgers", name = "Burgers"),
        MenuCategory(id = "fries-sides", name = "Fries & Sides"),
        MenuCategory(id = "beverages", name = "Beverages"),
    )

    val DEFAULT_ITEMS: Map<String, List<MenuItem>> = mapOf(
        "burgers" to listOf(
            MenuItem(
                id = "burger-bigmac",
                categoryId = "burgers",
                name = "Big Mac",
                priceFormatted = "$4.59",
                calories = 540,
                imageUrl = "https://example.test/menu/big-mac.png",
            ),
        ),
        "fries-sides" to listOf(
            MenuItem(
                id = "side-fries-md",
                categoryId = "fries-sides",
                name = "Medium Fries",
                priceFormatted = "$2.79",
                calories = 320,
                imageUrl = "https://example.test/menu/fries-md.png",
            ),
        ),
        "beverages" to listOf(
            MenuItem(
                id = "bev-coke-md",
                categoryId = "beverages",
                name = "Medium Coca-Cola",
                priceFormatted = "$1.99",
                calories = 200,
                imageUrl = "https://example.test/menu/coke-md.png",
            ),
        ),
    )

    val DEFAULT_CART = CartSummary(itemCount = 0, total = "$0.00")

    val DEFAULT_CONTENT = OrderContent(
        categories = DEFAULT_CATEGORIES,
        featuredItems = emptyList(),
        itemsByCategory = DEFAULT_ITEMS,
        cartSummary = DEFAULT_CART,
    )
}
