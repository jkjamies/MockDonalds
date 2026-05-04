package com.mockdonalds.app.features.kiosk.order.presentation

sealed class KioskOrderEvent {
    data class CategorySelected(val id: String) : KioskOrderEvent()
    data class ItemTapped(val id: String) : KioskOrderEvent()
    data object ScanOfferPressed : KioskOrderEvent()
    data object BackPressed : KioskOrderEvent()
    data object CartPressed : KioskOrderEvent()
    data object CancelOrderPressed : KioskOrderEvent()
    data object PayPressed : KioskOrderEvent()
}
