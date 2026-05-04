package com.mockdonalds.app.features.mobile.rewards.domain

import com.mockdonalds.app.features.mobile.rewards.api.domain.HistoryEntry
import com.mockdonalds.app.features.mobile.rewards.api.domain.RewardsProgress
import com.mockdonalds.app.features.mobile.rewards.api.domain.VaultSpecial
import kotlinx.coroutines.flow.Flow

interface RewardsRepository {
    fun getRewardsProgress(): Flow<RewardsProgress>
    fun getVaultSpecials(): Flow<List<VaultSpecial>>
    fun getHistory(): Flow<List<HistoryEntry>>
}
