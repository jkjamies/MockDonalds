package com.mockdonalds.app.features.order.domain

import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.FeaturedItem
import com.mockdonalds.app.features.order.api.domain.MenuCategory
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getMenuCategories(): Flow<List<MenuCategory>>
    fun getFeaturedItems(): Flow<List<FeaturedItem>>
    fun getCartSummary(): Flow<CartSummary>
}
