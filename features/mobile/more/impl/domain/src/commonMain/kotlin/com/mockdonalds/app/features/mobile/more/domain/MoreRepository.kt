package com.mockdonalds.app.features.mobile.more.domain

import com.mockdonalds.app.features.mobile.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.mobile.more.api.domain.UserProfile
import kotlinx.coroutines.flow.Flow

interface MoreRepository {
    fun getUserProfile(): Flow<UserProfile>
    fun getMenuItems(): Flow<List<MoreMenuItem>>
}
