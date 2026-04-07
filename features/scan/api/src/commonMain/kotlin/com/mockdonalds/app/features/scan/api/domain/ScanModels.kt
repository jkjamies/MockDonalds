package com.mockdonalds.app.features.scan.api.domain

data class MemberInfo(
    val memberStatus: String,
    val qrCodeUrl: String,
)

data class ScanRewardsProgress(
    val currentPoints: Int,
    val pointsToNextReward: Int,
    val progressFraction: Float,
    val message: String,
)

data class ScanContent(
    val memberInfo: MemberInfo,
    val rewardsProgress: ScanRewardsProgress,
)
