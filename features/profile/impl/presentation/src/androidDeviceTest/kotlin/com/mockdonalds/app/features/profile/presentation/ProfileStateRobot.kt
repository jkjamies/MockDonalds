package com.mockdonalds.app.features.profile.presentation

import com.mockdonalds.app.core.test.StateRobot

class ProfileStateRobot : StateRobot<ProfileUiState, ProfileEvent>() {

    override fun defaultState() = ProfileUiState(
        name = "Night Owl",
        email = "gourmet@night.com",
        tier = "Gold",
        points = "4,280 pts",
        avatarUrl = "",
        memberSince = "Member since 2024",
        eventSink = createEventSink(),
    )

    fun stateWithName(name: String) = defaultState().copy(
        name = name,
        eventSink = createEventSink(),
    )
}
