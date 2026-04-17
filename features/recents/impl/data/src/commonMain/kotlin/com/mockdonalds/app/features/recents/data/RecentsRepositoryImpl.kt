package com.mockdonalds.app.features.recents.data

import com.mockdonalds.app.features.recents.api.domain.RecentItem
import com.mockdonalds.app.features.recents.domain.RecentsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
@Inject
class RecentsRepositoryImpl : RecentsRepository {
    override fun getRecentItems(): Flow<List<RecentItem>> = flowOf(
        listOf(
            RecentItem(
                id = "1",
                name = "Big Mac Combo",
                description = "Combo Meal",
                relativeTime = "2 days ago",
                imageUrl = null,
            ),
            RecentItem(
                id = "2",
                name = "McFlurry Oreo",
                description = "Dessert",
                relativeTime = "Last week",
                imageUrl = null,
            ),
            RecentItem(
                id = "3",
                name = "10 pc. Chicken McNuggets",
                description = "Chicken",
                relativeTime = "2 weeks ago",
                imageUrl = null,
            ),
            RecentItem(
                id = "4",
                name = "Medium Fries",
                description = "Sides",
                relativeTime = "1 month ago",
                imageUrl = null,
            ),
            RecentItem(
                id = "5",
                name = "Large Iced Coffee",
                description = "Beverages",
                relativeTime = "1 month ago",
                imageUrl = null,
            ),
        ),
    )
}
