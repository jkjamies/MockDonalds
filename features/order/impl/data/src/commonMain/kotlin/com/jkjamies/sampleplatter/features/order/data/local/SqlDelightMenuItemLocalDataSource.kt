package com.jkjamies.sampleplatter.features.order.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
import com.jkjamies.sampleplatter.features.order.api.domain.MenuItem
import com.jkjamies.sampleplatter.features.order.data.MenuItemQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class SqlDelightMenuItemLocalDataSource(
    private val queries: MenuItemQueries,
    private val dispatchers: StrataDispatchers,
) : MenuItemLocalDataSource {

    override fun observeByCategory(categoryId: String): Flow<List<MenuItem>> =
        queries.selectByCategory(
            categoryId = categoryId,
            mapper = { id, _, title, restaurantChain, imageUrl, servingSize, _ ->
                MenuItem(
                    id = id,
                    title = title,
                    restaurantChain = restaurantChain,
                    imageUrl = imageUrl,
                    servingSize = servingSize,
                    categoryId = categoryId,
                )
            },
        )
            .asFlow()
            .mapToList(dispatchers.io)

    override fun firstImageUrlByCategory(categoryId: String): String? =
        queries.selectFirstByCategory(
            categoryId = categoryId,
            mapper = { _, _, _, _, imageUrl, _, _ -> imageUrl },
        ).executeAsOneOrNull()

    override fun countByCategory(categoryId: String): Int =
        queries.countByCategory(categoryId).executeAsOne().toInt()

    override fun oldestCachedAtByCategory(categoryId: String): Long? =
        queries.oldestCachedAtByCategory(categoryId).executeAsOneOrNull()?.oldestCachedAt

    override fun replaceCategory(categoryId: String, items: List<MenuItem>, cachedAt: Long) {
        queries.transaction {
            queries.deleteByCategory(categoryId)
            items.forEach { item ->
                queries.insertItem(
                    id = item.id,
                    categoryId = categoryId,
                    title = item.title,
                    restaurantChain = item.restaurantChain,
                    imageUrl = item.imageUrl,
                    servingSize = item.servingSize,
                    cachedAt = cachedAt,
                )
            }
        }
    }
}
