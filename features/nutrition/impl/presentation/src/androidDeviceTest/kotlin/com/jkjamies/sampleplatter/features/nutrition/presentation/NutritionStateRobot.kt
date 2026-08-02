package com.jkjamies.sampleplatter.features.nutrition.presentation

import com.jkjamies.sampleplatter.core.test.StateRobot

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
