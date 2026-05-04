package com.mockdonalds.app.features.kiosk.identify.api.domain

data class IdentifyContent(
    val skipEnabled: Boolean,
    val phoneEntryEnabled: Boolean,
    val qrScannerEnabled: Boolean,
    val countryDialCode: String,
)

sealed class IdentifyResult {
    data class Identified(val accountId: String) : IdentifyResult()
    data object GuestSession : IdentifyResult()
    data class Failed(val reason: String) : IdentifyResult()
}
