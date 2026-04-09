package com.mockdonalds.app.features.rewards.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.rewards.api.domain.HistoryEntry
import com.mockdonalds.app.features.rewards.api.domain.RewardsProgress
import com.mockdonalds.app.features.rewards.api.domain.VaultSpecial

class RewardsStateRobot : StateRobot<RewardsUiState, RewardsEvent>() {

    override fun defaultState() = RewardsUiState(
        progress = RewardsProgress(
            currentPoints = 1250,
            nextRewardName = "Free Fries",
            pointsToNextReward = 250,
            progressFraction = 0.83f,
        ),
        vaultSpecials = listOf(
            VaultSpecial(id = "1", title = "Secret Burger", pointsCost = "2000 PTS", imageUrl = "", tag = "LIMITED", isFeatured = true),
            VaultSpecial(id = "2", title = "Gold Shake", pointsCost = "1500 PTS", imageUrl = "", tag = null, isFeatured = false),
        ),
        history = listOf(
            HistoryEntry(id = "1", title = "Big Mac Meal", subtitle = "Yesterday", points = "+150", isPositive = true, icon = "🍔"),
        ),
        eventSink = createEventSink(),
    )

    fun stateWithNoProgress() = defaultState().copy(
        progress = null,
        eventSink = createEventSink(),
    )

    fun stateWithEmptyVault() = defaultState().copy(
        vaultSpecials = emptyList(),
        eventSink = createEventSink(),
    )

    fun stateWithEmptyHistory() = defaultState().copy(
        history = emptyList(),
        eventSink = createEventSink(),
    )
}
