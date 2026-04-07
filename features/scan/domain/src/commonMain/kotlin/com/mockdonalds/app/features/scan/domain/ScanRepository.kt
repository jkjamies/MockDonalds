package com.mockdonalds.app.features.scan.domain

import com.mockdonalds.app.features.scan.api.domain.MemberInfo
import com.mockdonalds.app.features.scan.api.domain.ScanRewardsProgress
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun getMemberInfo(): Flow<MemberInfo>
    fun getRewardsProgress(): Flow<ScanRewardsProgress>
}
