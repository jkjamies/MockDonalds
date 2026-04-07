package com.mockdonalds.app.features.rewards.test

import com.mockdonalds.app.features.rewards.api.domain.GetRewardsContent
import com.mockdonalds.app.features.rewards.api.domain.HistoryEntry
import com.mockdonalds.app.features.rewards.api.domain.RewardsContent
import com.mockdonalds.app.features.rewards.api.domain.RewardsProgress
import com.mockdonalds.app.features.rewards.api.domain.VaultSpecial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetRewardsContent(
    initial: RewardsContent = DEFAULT,
) : GetRewardsContent() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<RewardsContent> = _content

    fun emit(content: RewardsContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = RewardsContent(
            progress = RewardsProgress(
                currentPoints = 1000,
                nextRewardName = "Test Reward",
                pointsToNextReward = 500,
                progressFraction = 0.67f,
            ),
            vaultSpecials = listOf(
                VaultSpecial(
                    id = "1",
                    title = "Test Special",
                    pointsCost = "500 PTS",
                    imageUrl = "",
                    tag = "EXCLUSIVE",
                    isFeatured = true,
                ),
            ),
            history = listOf(
                HistoryEntry(id = "1", title = "Test Order", subtitle = "Today", points = "+100", isPositive = true, icon = "🍽️"),
            ),
        )
    }
}
