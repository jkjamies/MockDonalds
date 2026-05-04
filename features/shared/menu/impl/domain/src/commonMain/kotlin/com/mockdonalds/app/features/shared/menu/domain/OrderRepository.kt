package com.mockdonalds.app.features.shared.menu.domain

import com.mockdonalds.app.features.shared.menu.api.domain.CartSummary
import com.mockdonalds.app.features.shared.menu.api.domain.FeaturedItem
import com.mockdonalds.app.features.shared.menu.api.domain.MenuCategory
import com.mockdonalds.app.features.shared.menu.api.domain.MenuItem
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getMenuCategories(): Flow<List<MenuCategory>>
    fun getFeaturedItems(): Flow<List<FeaturedItem>>
    fun getItemsByCategory(): Flow<Map<String, List<MenuItem>>>
    fun getCartSummary(): Flow<CartSummary>
}
