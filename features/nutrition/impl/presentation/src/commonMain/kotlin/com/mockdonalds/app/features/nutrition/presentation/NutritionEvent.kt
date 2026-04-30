package com.mockdonalds.app.features.nutrition.presentation

sealed class NutritionEvent {
    data object BackClicked : NutritionEvent()
}
