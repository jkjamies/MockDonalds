package com.mockdonalds.app.features.rewards.domain

import com.mockdonalds.app.features.rewards.api.domain.HistoryEntry
import com.mockdonalds.app.features.rewards.api.domain.RewardsProgress
import com.mockdonalds.app.features.rewards.api.domain.VaultSpecial
import kotlinx.coroutines.flow.Flow

interface RewardsRepository {
    fun getRewardsProgress(): Flow<RewardsProgress>
    fun getVaultSpecials(): Flow<List<VaultSpecial>>
    fun getHistory(): Flow<List<HistoryEntry>>
}
