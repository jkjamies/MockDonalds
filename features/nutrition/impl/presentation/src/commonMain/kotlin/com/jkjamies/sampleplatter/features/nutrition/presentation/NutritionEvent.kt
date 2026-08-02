package com.jkjamies.sampleplatter.features.nutrition.presentation

sealed class NutritionEvent {
    data object BackClicked : NutritionEvent()
}
