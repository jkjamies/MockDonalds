package com.mockdonalds.app.features.recents.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.recents.api.domain.RecentItem

class RecentsStateRobot : StateRobot<RecentsUiState, RecentsEvent>() {

    override fun defaultState() = RecentsUiState.Success(
        items = listOf(
            RecentItem("1", "Big Mac Combo", "Combo Meal", "2 days ago", null),
            RecentItem("2", "McFlurry Oreo", "Dessert", "Last week", null),
        ),
        eventSink = createEventSink(),
    )
    
    fun emptyState() = RecentsUiState.Empty(
        eventSink = createEventSink(),
    )
}