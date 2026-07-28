package com.jkjamies.sampleplatter.features.home.domain

import com.jkjamies.sampleplatter.core.logger.featureLogger
import com.jkjamies.sampleplatter.features.home.api.domain.GetHomeContent
import com.jkjamies.sampleplatter.features.home.api.domain.HomeContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private val log = featureLogger("Home")

@ContributesBinding(AppScope::class)
class GetHomeContentImpl(
    private val repository: HomeRepository,
) : GetHomeContent() {
    override fun createObservable(params: Unit): Flow<HomeContent> {
        log.d { "Building HomeContent flow" }
        return combine(
            repository.getUserName(),
            repository.getHeroPromotion(),
            repository.getRecentCravings(),
            repository.getExploreItems(),
        ) { userName, hero, cravings, explore ->
            log.v { "HomeContent assembled (explore=${explore.size}, cravings=${cravings.size})" }
            HomeContent(
                userName = userName,
                heroPromotion = hero,
                recentCravings = cravings,
                exploreItems = explore,
            )
        }
    }
}
