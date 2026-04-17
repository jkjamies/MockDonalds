package com.mockdonalds.app.features.recents.domain

import com.mockdonalds.app.features.recents.api.domain.RecentItem
import kotlinx.coroutines.flow.Flow

interface RecentsRepository {
    fun getRecentItems(): Flow<List<RecentItem>>
}
