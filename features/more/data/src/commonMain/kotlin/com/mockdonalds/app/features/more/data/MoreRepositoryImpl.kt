@file:Suppress("MaxLineLength") // URLs in fake data

package com.mockdonalds.app.features.more.data

import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.domain.UserProfile
import com.mockdonalds.app.features.more.domain.MoreRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class MoreRepositoryImpl : MoreRepository {

    override fun getUserProfile(): Flow<UserProfile> = flowOf(
        UserProfile(
            name = "Alex Grayson",
            tier = "Gold Member",
            points = "1,240 pts",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBFVaei4-VUjNq0l_ZGz6rArkAkp-dfveXXlPEl_pcZxRm8PiBdB1Ou7OAx2lEYYD9sPqyC0Rw34pGxWZ_0NWGUvTRio8O5wqu0oobfn5TWDTglgEvOWyPDn6rtcYPzMO5PK6I5IvCQfhWru2-mYEE8s45hGeUZIAQd7mvcCjvhIV9c4vrSetu5K1hBrZmIUQu2TND-knQQzGfp7U_8aQ0JPl_FgLwTXM47MsuJKCkkhDodQ_0vCMZbkh6SRx_s4Qmwlk14O8d1sgQ",
        ),
    )

    override fun getMenuItems(): Flow<List<MoreMenuItem>> = flowOf(
        listOf(
            MoreMenuItem(id = "1", icon = "🕒", title = "Recents"),
            MoreMenuItem(id = "2", icon = "📍", title = "Locations"),
            MoreMenuItem(id = "3", icon = "🥗", title = "Nutrition"),
            MoreMenuItem(id = "4", icon = "❓", title = "Help"),
            MoreMenuItem(id = "5", icon = "💼", title = "Careers"),
        ),
    )
}
