package com.mockdonalds.app.features.order.data.local

import com.mockdonalds.app.features.order.api.domain.MenuItem
import kotlinx.coroutines.flow.Flow

interface MenuItemLocalDataSource {
    fun observeByCategory(categoryId: String): Flow<List<MenuItem>>
    fun firstImageUrlByCategory(categoryId: String): String?
    fun countByCategory(categoryId: String): Int
    fun oldestCachedAtByCategory(categoryId: String): Long?
    fun replaceCategory(categoryId: String, items: List<MenuItem>, cachedAt: Long)
}
