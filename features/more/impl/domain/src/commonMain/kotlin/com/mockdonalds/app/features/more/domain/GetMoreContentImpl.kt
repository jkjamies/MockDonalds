package com.mockdonalds.app.features.more.domain

import com.mockdonalds.app.features.more.api.domain.GetMoreContent
import com.mockdonalds.app.features.more.api.domain.MoreContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ContributesBinding(AppScope::class)
class GetMoreContentImpl(
    private val repository: MoreRepository,
) : GetMoreContent() {
    override fun createObservable(params: Unit): Flow<MoreContent> {
        return combine(
            repository.getUserProfile(),
            repository.getMenuItems(),
        ) { profile, items ->
            MoreContent(
                userProfile = profile,
                menuItems = items,
            )
        }
    }
}
