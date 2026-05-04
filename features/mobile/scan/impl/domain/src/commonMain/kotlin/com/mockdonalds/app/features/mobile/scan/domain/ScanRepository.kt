package com.mockdonalds.app.features.mobile.scan.domain

import com.mockdonalds.app.features.mobile.scan.api.domain.MemberInfo
import com.mockdonalds.app.features.mobile.scan.api.domain.ScanRewardsProgress
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun getMemberInfo(): Flow<MemberInfo>
    fun getRewardsProgress(): Flow<ScanRewardsProgress>
}
