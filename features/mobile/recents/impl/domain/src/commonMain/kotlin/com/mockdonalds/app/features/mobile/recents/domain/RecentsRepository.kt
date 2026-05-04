package com.mockdonalds.app.features.mobile.recents.domain

import com.mockdonalds.app.features.mobile.recents.api.domain.RecentItem
import kotlinx.coroutines.flow.Flow

interface RecentsRepository {
    fun getRecentItems(): Flow<List<RecentItem>>
}
