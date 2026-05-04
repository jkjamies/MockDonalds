package com.mockdonalds.app.features.kiosk.identify.presentation

import com.slack.circuit.runtime.CircuitUiState

data class IdentifyUiState(
    val phoneInput: String,
    val isSubmitting: Boolean,
    val errorMessage: String?,
    val skipEnabled: Boolean,
    val qrScannerEnabled: Boolean,
    val countryDialCode: String,
    val eventSink: (IdentifyEvent) -> Unit,
) : CircuitUiState
