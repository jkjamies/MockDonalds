package com.mockdonalds.app.features.kiosk.attract.presentation

sealed class AttractEvent {
    data object TouchToOrder : AttractEvent()
    data class IndexChanged(val index: Int) : AttractEvent()
}
