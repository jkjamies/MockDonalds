package com.mockdonalds.app.features.home.test

import com.mockdonalds.app.features.home.api.domain.Craving
import com.mockdonalds.app.features.home.api.domain.ExploreItem
import com.mockdonalds.app.features.home.api.domain.GetHomeContent
import com.mockdonalds.app.features.home.api.domain.HeroPromotion
import com.mockdonalds.app.features.home.api.domain.HomeContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGetHomeContent(
    initial: HomeContent = DEFAULT,
) : GetHomeContent() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<HomeContent> = _content

    fun emit(content: HomeContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = HomeContent(
            userName = "TestUser",
            heroPromotion = HeroPromotion(
                title = "Test Promo",
                description = "Description",
                tag = "NEW",
                imageUrl = "",
                ctaText = "Order Now",
            ),
            recentCravings = listOf(
                Craving(id = "1", title = "Big Mac", subtitle = "Classic", imageUrl = ""),
            ),
            exploreItems = listOf(
                ExploreItem(id = "1", icon = "star", title = "Deals", subtitle = "Save more"),
            ),
        )
    }
}
