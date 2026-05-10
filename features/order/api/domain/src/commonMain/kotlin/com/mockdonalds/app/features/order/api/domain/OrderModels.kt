package com.mockdonalds.app.features.order.api.domain

data class MenuItem(
    val id: String,
    val title: String,
    val restaurantChain: String,
    val imageUrl: String,
    val servingSize: String?,
    val categoryId: String,
)

data class CategoryPreview(
    val id: String,
    val name: String,
    val firstItemImageUrl: String?,
    val itemCount: Int,
)

data class CategoryDetailContent(
    val categoryId: String,
    val categoryName: String,
    val items: List<MenuItem>,
)

data class CartSummary(
    val itemCount: Int,
    val total: String,
)

data class OrderContent(
    val categoryPreviews: List<CategoryPreview>,
    val cartSummary: CartSummary,
)
