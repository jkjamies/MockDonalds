package com.mockdonalds.app.features.kiosk.identify.presentation

sealed class IdentifyEvent {
    data class DigitPressed(val digit: Char) : IdentifyEvent()
    data object DeletePressed : IdentifyEvent()
    data object SubmitPhonePressed : IdentifyEvent()
    data class QrCodeDetected(val payload: String) : IdentifyEvent()
    data object SkipPressed : IdentifyEvent()
    data object DismissError : IdentifyEvent()
}
