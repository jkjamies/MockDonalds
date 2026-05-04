package com.mockdonalds.app.features.mobile.nutrition.presentation

sealed class NutritionEvent {
    data object BackClicked : NutritionEvent()
}
