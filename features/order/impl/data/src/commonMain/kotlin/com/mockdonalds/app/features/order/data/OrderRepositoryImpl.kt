package com.mockdonalds.app.features.order.data

import com.mockdonalds.app.core.logger.featureLogger
import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.CategoryPreview
import com.mockdonalds.app.features.order.api.domain.MenuItem
import com.mockdonalds.app.features.order.data.local.MenuItemLocalDataSource
import com.mockdonalds.app.features.order.data.remote.MenuRemoteDataSource
import com.mockdonalds.app.features.order.data.remote.toMenuItem
import com.mockdonalds.app.features.order.domain.OrderRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@ContributesBinding(AppScope::class)
class OrderRepositoryImpl(
    private val remote: MenuRemoteDataSource,
    private val local: MenuItemLocalDataSource,
) : OrderRepository {

    private val logger = featureLogger("OrderRepository")

    override fun getCategoryPreviews(): Flow<List<CategoryPreview>> {
        val cacheFlow: Flow<List<CategoryPreview>> = combine(
            CATEGORIES.map { category ->
                local.observeByCategory(category.id).map { items ->
                    CategoryPreview(
                        id = category.id,
                        name = category.displayName,
                        firstItemImageUrl = items.firstOrNull()?.imageUrl,
                        itemCount = items.size,
                    )
                }
            },
        ) { previews -> previews.toList() }

        val refreshSideEffect: Flow<List<CategoryPreview>> = flow {
            CATEGORIES.forEach { refreshIfStale(it) }
        }

        return merge(cacheFlow, refreshSideEffect)
    }

    override fun getMenuItemsByCategory(categoryId: String): Flow<List<MenuItem>> {
        val category = CATEGORIES.firstOrNull { it.id == categoryId }
            ?: return flowOf(emptyList())

        val cacheFlow = local.observeByCategory(categoryId)

        val refreshSideEffect: Flow<List<MenuItem>> = flow {
            refreshIfStale(category)
        }

        return merge(cacheFlow, refreshSideEffect)
    }

    override fun categoryName(categoryId: String): String? =
        CATEGORIES.firstOrNull { it.id == categoryId }?.displayName

    override fun getCartSummary(): Flow<CartSummary> = flowOf(
        CartSummary(itemCount = 2, total = "\$36.00"),
    )

    private suspend fun refreshIfStale(category: Category) {
        val oldest = local.oldestCachedAtByCategory(category.id)
        val now = Clock.System.now().toEpochMilliseconds()
        val isStale = oldest == null || (now - oldest) > TTL_MILLIS
        if (!isStale) return

        runCatching { remote.searchMenuItems(category.query) }
            .onSuccess { dtos ->
                val items = dtos.map { it.toMenuItem(category.id) }
                local.replaceCategory(category.id, items, now)
            }
            .onFailure { error ->
                logger.e(error) {
                    "Spoonacular fetch failed for categoryId=${category.id} query=${category.query}"
                }
            }
    }

    private data class Category(
        val id: String,
        val displayName: String,
        val query: String,
    )

    private companion object {
        val TTL_MILLIS: Long = 24.hours.inWholeMilliseconds

        val CATEGORIES = listOf(
            Category(id = "featured", displayName = "Featured", query = "popular"),
            Category(id = "burgers", displayName = "Burgers", query = "burger"),
            Category(id = "chicken", displayName = "Chicken", query = "chicken sandwich"),
            Category(id = "sides", displayName = "Sides", query = "fries"),
            Category(id = "drinks", displayName = "Drinks", query = "soda"),
            Category(id = "desserts", displayName = "Desserts", query = "ice cream"),
        )
    }
}
