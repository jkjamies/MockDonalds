package com.jkjamies.sampleplatter.features.more.domain

import com.jkjamies.sampleplatter.features.more.api.domain.MoreMenuItem
import com.jkjamies.sampleplatter.features.more.api.domain.UserProfile
import kotlinx.coroutines.flow.Flow

interface MoreRepository {
    fun getUserProfile(): Flow<UserProfile>
    fun getMenuItems(): Flow<List<MoreMenuItem>>
}
