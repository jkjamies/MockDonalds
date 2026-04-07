package com.mockdonalds.app.features.more.presentation

import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.domain.UserProfile
import com.slack.circuit.runtime.CircuitUiState

data class MoreUiState(
    val userProfile: UserProfile? = null,
    val menuItems: List<MoreMenuItem> = emptyList(),
    val eventSink: (MoreEvent) -> Unit,
) : CircuitUiState

sealed interface MoreEvent {
    data object ProfileClicked : MoreEvent
    data class MenuItemClicked(val id: String) : MoreEvent
}
