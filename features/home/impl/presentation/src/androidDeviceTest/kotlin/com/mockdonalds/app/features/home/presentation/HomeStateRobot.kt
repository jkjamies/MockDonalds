package com.mockdonalds.app.features.home.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.home.api.domain.Craving
import com.mockdonalds.app.features.home.api.domain.ExploreItem
import com.mockdonalds.app.features.home.api.domain.HeroPromotion

class HomeStateRobot : StateRobot<HomeUiState, HomeEvent>() {

    override fun defaultState() = HomeUiState(
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
            ExploreItem(id = "2", icon = "gift", title = "Gifts", subtitle = "Share joy"),
        ),
        eventSink = createEventSink(),
    )

    fun stateWithNoPromotion() = defaultState().copy(
        heroPromotion = null,
        eventSink = createEventSink(),
    )

    fun stateWithEmptyCravings() = defaultState().copy(
        recentCravings = emptyList(),
        eventSink = createEventSink(),
    )

    fun stateWithUserName(name: String) = defaultState().copy(
        userName = name,
        eventSink = createEventSink(),
    )
}
