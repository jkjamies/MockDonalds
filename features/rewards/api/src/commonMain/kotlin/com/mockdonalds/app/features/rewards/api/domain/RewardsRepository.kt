package com.mockdonalds.app.features.rewards.api.domain

import kotlinx.coroutines.flow.Flow

interface RewardsRepository {
    fun getRewardsPoints(): Flow<Int>
    fun getAvailableRewards(): Flow<List<Reward>>
}

data class Reward(
    val id: String,
    val name: String,
    val pointsCost: Int,
    val imageUrl: String,
)
