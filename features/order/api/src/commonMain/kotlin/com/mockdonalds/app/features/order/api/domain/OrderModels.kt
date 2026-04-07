package com.mockdonalds.app.features.order.api.domain

data class MenuCategory(
    val id: String,
    val name: String,
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

data class CartSummary(
    val itemCount: Int,
    val total: String,
)

data class OrderContent(
    val categories: List<MenuCategory>,
    val featuredItems: List<FeaturedItem>,
    val cartSummary: CartSummary,
)
