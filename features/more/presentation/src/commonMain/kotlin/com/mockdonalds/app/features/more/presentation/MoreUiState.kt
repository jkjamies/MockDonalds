package com.mockdonalds.app.features.more.presentation

import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.domain.UserProfile
import com.slack.circuit.runtime.CircuitUiState

data class MoreUiState(
    val userProfile: UserProfile? = null,
    val menuItems: List<MoreMenuItem> = emptyList(),
    val loginSheet: LoginSheetState? = null,
    val eventSink: (MoreEvent) -> Unit,
) : CircuitUiState

data class LoginSheetState(
    val logoUrl: String = "",
    val email: String = "",
)

sealed class MoreEvent {
    data object ProfileClicked : MoreEvent()
    data class MenuItemClicked(val id: String) : MoreEvent()
    data class LoginEmailChanged(val value: String) : MoreEvent()
    data object LoginSignInConfirmed : MoreEvent()
    data object LoginSheetDismissed : MoreEvent()
}
