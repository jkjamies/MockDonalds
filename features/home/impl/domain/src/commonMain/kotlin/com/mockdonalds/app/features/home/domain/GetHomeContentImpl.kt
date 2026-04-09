package com.mockdonalds.app.features.home.domain

import com.mockdonalds.app.features.home.api.domain.GetHomeContent
import com.mockdonalds.app.features.home.api.domain.HomeContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ContributesBinding(AppScope::class)
class GetHomeContentImpl(
    private val repository: HomeRepository,
) : GetHomeContent() {
    override fun createObservable(params: Unit): Flow<HomeContent> {
        return combine(
            repository.getUserName(),
            repository.getHeroPromotion(),
            repository.getRecentCravings(),
            repository.getExploreItems(),
        ) { userName, hero, cravings, explore ->
            HomeContent(
                userName = userName,
                heroPromotion = hero,
                recentCravings = cravings,
                exploreItems = explore,
            )
        }
    }
}
