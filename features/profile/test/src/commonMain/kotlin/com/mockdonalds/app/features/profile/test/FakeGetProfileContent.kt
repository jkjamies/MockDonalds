package com.mockdonalds.app.features.profile.test

import com.mockdonalds.app.features.profile.api.domain.GetProfileContent
import com.mockdonalds.app.features.profile.api.domain.ProfileContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class FakeGetProfileContent(
    private val content: ProfileContent = ProfileContent(
        name = "Night Owl",
        email = "gourmet@night.com",
        tier = "Gold",
        points = "4,280 pts",
        avatarUrl = "",
        memberSince = "Member since 2024",
    ),
) : GetProfileContent() {

    override fun createObservable(params: Unit): Flow<ProfileContent> {
        return flowOf(content)
    }
}
