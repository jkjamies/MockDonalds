package com.mockdonalds.app.features.profile.presentation

import com.slack.circuit.runtime.CircuitUiState

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val tier: String = "",
    val points: String = "",
    val avatarUrl: String = "",
    val memberSince: String = "",
    val eventSink: (ProfileEvent) -> Unit,
) : CircuitUiState

sealed class ProfileEvent {
    data object LogoutClicked : ProfileEvent()
}
