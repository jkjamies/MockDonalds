package com.mockdonalds.app.features.order.api.domain

data class MenuCategory(
    val id: String,
    val name: String,
    val iconUrl: String? = null,
)

data class FeaturedItem(
    val id: String,
    val title: String,
    val price: String,
    val description: String,
    val imageUrl: String,
    val tag: String,
    val isPrimary: Boolean,
)

data class MenuItem(
    val id: String,
    val categoryId: String,
    val name: String,
    val priceFormatted: String,
    val calories: Int?,
    val imageUrl: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
)

data class CartSummary(
    val itemCount: Int,
    val total: String,
)

data class OrderContent(
    val categories: List<MenuCategory>,
    val featuredItems: List<FeaturedItem>,
    val itemsByCategory: Map<String, List<MenuItem>> = emptyMap(),
    val cartSummary: CartSummary,
)
