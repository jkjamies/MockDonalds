package com.mockdonalds.app.features.more.domain

import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.domain.UserProfile
import kotlinx.coroutines.flow.Flow

interface MoreRepository {
    fun getUserProfile(): Flow<UserProfile>
    fun getMenuItems(): Flow<List<MoreMenuItem>>
}
