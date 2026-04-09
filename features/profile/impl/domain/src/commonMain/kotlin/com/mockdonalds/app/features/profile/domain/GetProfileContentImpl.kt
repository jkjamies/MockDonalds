package com.mockdonalds.app.features.profile.domain

import com.mockdonalds.app.features.profile.api.domain.GetProfileContent
import com.mockdonalds.app.features.profile.api.domain.ProfileContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class GetProfileContentImpl @Inject constructor(
    private val repository: ProfileRepository,
) : GetProfileContent() {

    override fun createObservable(params: Unit): Flow<ProfileContent> {
        return repository.getProfile()
    }
}
