package com.mockdonalds.app.features.order.domain

import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.CategoryPreview
import com.mockdonalds.app.features.order.api.domain.MenuItem
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getCategoryPreviews(): Flow<List<CategoryPreview>>
    fun getMenuItemsByCategory(categoryId: String): Flow<List<MenuItem>>
    fun categoryName(categoryId: String): String?
    fun getCartSummary(): Flow<CartSummary>
}
