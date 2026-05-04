package com.mockdonalds.app.features.mobile.nutrition.presentation

import com.mockdonalds.app.core.test.StateRobot

class NutritionStateRobot : StateRobot<NutritionUiState, NutritionEvent>() {

    override fun defaultState() = NutritionUiState(
        url = "https://example.test/nutrition",
        eventSink = createEventSink(),
    )

    fun loadingState() = NutritionUiState(
        url = null,
        eventSink = createEventSink(),
    )
}
