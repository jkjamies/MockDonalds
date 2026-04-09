package com.mockdonalds.app.features.rewards.domain

import com.mockdonalds.app.features.rewards.api.domain.GetRewardsContent
import com.mockdonalds.app.features.rewards.api.domain.RewardsContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ContributesBinding(AppScope::class)
class GetRewardsContentImpl(
    private val repository: RewardsRepository,
) : GetRewardsContent() {
    override fun createObservable(params: Unit): Flow<RewardsContent> {
        return combine(
            repository.getRewardsProgress(),
            repository.getVaultSpecials(),
            repository.getHistory(),
        ) { progress, specials, history ->
            RewardsContent(
                progress = progress,
                vaultSpecials = specials,
                history = history,
            )
        }
    }
}
