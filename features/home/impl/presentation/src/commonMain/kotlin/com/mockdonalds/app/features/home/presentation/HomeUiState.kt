package com.mockdonalds.app.features.home.presentation

import com.mockdonalds.app.features.home.api.domain.Craving
import com.mockdonalds.app.features.home.api.domain.ExploreItem
import com.mockdonalds.app.features.home.api.domain.HeroPromotion
import com.slack.circuit.runtime.CircuitUiState

data class HomeUiState(
    val userName: String = "",
    val heroPromotion: HeroPromotion? = null,
    val recentCravings: List<Craving> = emptyList(),
    val exploreItems: List<ExploreItem> = emptyList(),
    val eventSink: (HomeEvent) -> Unit,
) : CircuitUiState

sealed class HomeEvent {
    data object HeroCtaClicked : HomeEvent()
    data class CravingClicked(val id: String) : HomeEvent()
    data class ExploreItemClicked(val id: String) : HomeEvent()
}
