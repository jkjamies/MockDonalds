package com.jkjamies.sampleplatter.features.scan.domain

import com.jkjamies.sampleplatter.features.scan.api.domain.MemberInfo
import com.jkjamies.sampleplatter.features.scan.api.domain.ScanRewardsProgress
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun getMemberInfo(): Flow<MemberInfo>
    fun getRewardsProgress(): Flow<ScanRewardsProgress>
}
