package com.mockdonalds.app.features.more.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.domain.UserProfile

class MoreStateRobot : StateRobot<MoreUiState, MoreEvent>() {

    override fun defaultState() = MoreUiState(
        userProfile = UserProfile(
            name = "TestUser",
            tier = "Gold",
            points = "1,250 PTS",
            avatarUrl = "",
        ),
        menuItems = listOf(
            MoreMenuItem(id = "1", icon = "📍", title = "Find a Restaurant"),
            MoreMenuItem(id = "2", icon = "⚙️", title = "Settings"),
        ),
        eventSink = createEventSink(),
    )

    fun stateWithNoProfile() = defaultState().copy(
        userProfile = null,
        eventSink = createEventSink(),
    )

    fun stateWithEmptyMenu() = defaultState().copy(
        menuItems = emptyList(),
        eventSink = createEventSink(),
    )
}
