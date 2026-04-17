package com.mockdonalds.app.features.more.test

import com.mockdonalds.app.features.more.api.domain.GetMoreContent
import com.mockdonalds.app.features.more.api.domain.MoreContent
import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.domain.UserProfile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGetMoreContent(
    initial: MoreContent = DEFAULT,
) : GetMoreContent() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<MoreContent> = _content

    fun emit(content: MoreContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = MoreContent(
            userProfile = UserProfile(
                name = "Test User",
                tier = "Gold Member",
                points = "1,000 pts",
                avatarUrl = "",
            ),
            menuItems = listOf(
                MoreMenuItem(id = "1", icon = "🕒", title = "Recents"),
                MoreMenuItem(id = "2", icon = "📍", title = "Locations"),
            ),
        )
    }
}
