package com.mockdonalds.app.features.scan.presentation

import com.mockdonalds.app.features.scan.api.domain.MemberInfo
import com.mockdonalds.app.features.scan.api.domain.ScanRewardsProgress
import com.slack.circuit.runtime.CircuitUiState

data class ScanUiState(
    val memberInfo: MemberInfo? = null,
    val rewardsProgress: ScanRewardsProgress? = null,
    val eventSink: (ScanEvent) -> Unit,
) : CircuitUiState

sealed class ScanEvent {
    data object PayNowClicked : ScanEvent()
    data object ViewOffersClicked : ScanEvent()
}
