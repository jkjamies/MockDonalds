package com.mockdonalds.app.features.mobile.more.presentation

import com.mockdonalds.app.features.mobile.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.mobile.more.api.domain.UserProfile
import com.slack.circuit.runtime.CircuitUiState

data class MoreUiState(
    val userProfile: UserProfile? = null,
    val menuItems: List<MoreMenuItem> = emptyList(),
    val eventSink: (MoreEvent) -> Unit,
) : CircuitUiState

sealed class MoreEvent {
    data object ProfileClicked : MoreEvent()
    data class MenuItemClicked(val id: String) : MoreEvent()
}
