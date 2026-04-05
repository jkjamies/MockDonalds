package com.mockdonalds.app.features.order.api.domain

import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getMenuCategories(): Flow<List<MenuCategory>>
}

data class MenuCategory(
    val id: String,
    val name: String,
    val imageUrl: String,
)
