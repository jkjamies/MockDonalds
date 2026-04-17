package com.mockdonalds.app.features.recents.presentation

sealed class RecentsEvent {
    data class OnItemTapped(val id: String) : RecentsEvent()
    data object OnBackTapped : RecentsEvent()
}
