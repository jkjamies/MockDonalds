package com.mockdonalds.app.features.rewards.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.rewards.api.domain.HistoryEntry
import com.mockdonalds.app.features.rewards.api.domain.RewardsContent
import com.mockdonalds.app.features.rewards.api.domain.RewardsProgress
import com.mockdonalds.app.features.rewards.api.domain.VaultSpecial
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetRewardsContentImplTest : BehaviorSpec({

    Given("a rewards content interactor with repository data") {
        val progress = MutableStateFlow(
            RewardsProgress(
                currentPoints = 1000,
                nextRewardName = "Test Reward",
                pointsToNextReward = 500,
                progressFraction = 0.67f,
            ),
        )
        val vaultSpecials = MutableStateFlow(
            listOf(
                VaultSpecial(
                    id = "1",
                    title = "Test Special",
                    pointsCost = "500 PTS",
                    imageUrl = "",
                    tag = "EXCLUSIVE",
                    isFeatured = true,
                ),
            ),
        )
        val history = MutableStateFlow(
            listOf(
                HistoryEntry(id = "1", title = "Test Order", subtitle = "Today", points = "+100", isPositive = true, icon = "🍽️"),
            ),
        )

        val repository = object : RewardsRepository {
            override fun getRewardsProgress(): Flow<RewardsProgress> = progress
            override fun getVaultSpecials(): Flow<List<VaultSpecial>> = vaultSpecials
            override fun getHistory(): Flow<List<HistoryEntry>> = history
        }

        val interactor = GetRewardsContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it should combine all repository data into RewardsContent") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content shouldBe RewardsContent(
                        progress = progress.value,
                        vaultSpecials = vaultSpecials.value,
                        history = history.value,
                    )
                }
            }
        }

        When("the repository progress updates") {
            Then("it should emit updated content") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem()
                    progress.value = progress.value.copy(currentPoints = 2000)
                    val updated = awaitItem()
                    updated.progress.currentPoints shouldBe 2000
                }
            }
        }
    }
})
