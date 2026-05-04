package com.mockdonalds.app.features.kiosk.identify.presentation

import com.mockdonalds.app.core.test.StateRobot

class IdentifyStateRobot : StateRobot<IdentifyUiState, IdentifyEvent>() {

    override fun defaultState() = IdentifyUiState(
        phoneInput = "",
        isSubmitting = false,
        errorMessage = null,
        skipEnabled = true,
        qrScannerEnabled = true,
        countryDialCode = "+1",
        eventSink = createEventSink(),
    )

    fun submittingState() = defaultState().copy(isSubmitting = true)

    fun errorState() = defaultState().copy(errorMessage = "Enter at least 7 digits")

    fun skipDisabledState() = defaultState().copy(skipEnabled = false)

    fun qrDisabledState() = defaultState().copy(qrScannerEnabled = false)
}
