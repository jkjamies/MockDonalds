package com.mockdonalds.app.features.profile.data

import com.mockdonalds.app.features.profile.api.domain.ProfileContent
import com.mockdonalds.app.features.profile.domain.ProfileRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class ProfileRepositoryImpl @Inject constructor() : ProfileRepository {

    @Suppress("MaxLineLength")
    private val defaultAvatarUrl =
        "https://lh3.googleusercontent.com/aida-public/" +
            "AB6AXuBGHJKuvdalEh9C2MFeJJajDpb0wfvW8e9JOHhkBIFomjQnT8C7BLplMUHfjVIOv6-" +
            "YxMrPnuOxDnSBArCfYp9wZkScBWqvxuYG4FZhXP2e5WpFnqLHkRnDFscjKVPX"

    override fun getProfile(): Flow<ProfileContent> = flowOf(
        ProfileContent(
            name = "Night Owl",
            email = "gourmet@night.com",
            tier = "Gold",
            points = "4,280 pts",
            avatarUrl = defaultAvatarUrl,
            memberSince = "Member since 2024",
        ),
    )
}
