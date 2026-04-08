package com.mockdonalds.app.features.rewards.api.domain

data class RewardsProgress(
    val currentPoints: Int,
    val nextRewardName: String,
    val pointsToNextReward: Int,
    val progressFraction: Float,
)

data class VaultSpecial(
    val id: String,
    val title: String,
    val pointsCost: String,
    val imageUrl: String,
    val tag: String?,
    val isFeatured: Boolean,
)

data class HistoryEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val points: String,
    val isPositive: Boolean,
    val icon: String,
)

data class RewardsContent(
    val progress: RewardsProgress,
    val vaultSpecials: List<VaultSpecial>,
    val history: List<HistoryEntry>,
)
