package com.mockdonalds.app.features.mobile.profile.domain

import com.mockdonalds.app.features.mobile.profile.api.domain.ProfileContent
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(): Flow<ProfileContent>
}
