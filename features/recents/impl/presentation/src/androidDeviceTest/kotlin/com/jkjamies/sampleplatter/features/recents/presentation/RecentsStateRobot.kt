package com.jkjamies.sampleplatter.features.recents.presentation

import com.jkjamies.sampleplatter.core.test.StateRobot
import com.jkjamies.sampleplatter.features.recents.api.domain.RecentItem

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