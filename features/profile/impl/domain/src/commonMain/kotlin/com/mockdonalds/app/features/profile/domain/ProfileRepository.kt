package com.mockdonalds.app.features.profile.domain

import com.mockdonalds.app.features.profile.api.domain.ProfileContent
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(): Flow<ProfileContent>
}
