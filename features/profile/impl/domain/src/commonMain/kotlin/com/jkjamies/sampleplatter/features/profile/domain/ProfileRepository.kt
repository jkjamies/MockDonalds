package com.jkjamies.sampleplatter.features.profile.domain

import com.jkjamies.sampleplatter.features.profile.api.domain.ProfileContent
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(): Flow<ProfileContent>
}
