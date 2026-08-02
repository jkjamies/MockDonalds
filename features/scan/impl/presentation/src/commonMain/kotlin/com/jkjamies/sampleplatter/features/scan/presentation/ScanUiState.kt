package com.jkjamies.sampleplatter.features.scan.presentation

import com.jkjamies.sampleplatter.features.scan.api.domain.MemberInfo
import com.jkjamies.sampleplatter.features.scan.api.domain.ScanRewardsProgress
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
