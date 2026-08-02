package com.jkjamies.sampleplatter.features.rewards.domain

import com.jkjamies.sampleplatter.features.rewards.api.domain.HistoryEntry
import com.jkjamies.sampleplatter.features.rewards.api.domain.RewardsProgress
import com.jkjamies.sampleplatter.features.rewards.api.domain.VaultSpecial
import kotlinx.coroutines.flow.Flow

interface RewardsRepository {
    fun getRewardsProgress(): Flow<RewardsProgress>
    fun getVaultSpecials(): Flow<List<VaultSpecial>>
    fun getHistory(): Flow<List<HistoryEntry>>
}
