package com.jkjamies.sampleplatter.features.recents.domain

import com.jkjamies.sampleplatter.features.recents.api.domain.RecentItem
import kotlinx.coroutines.flow.Flow

interface RecentsRepository {
    fun getRecentItems(): Flow<List<RecentItem>>
}
