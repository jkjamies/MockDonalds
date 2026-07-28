package com.jkjamies.sampleplatter.features.scan.domain

import com.jkjamies.sampleplatter.features.scan.api.domain.GetScanContent
import com.jkjamies.sampleplatter.features.scan.api.domain.ScanContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ContributesBinding(AppScope::class)
class GetScanContentImpl(
    private val repository: ScanRepository,
) : GetScanContent() {
    override fun createObservable(params: Unit): Flow<ScanContent> {
        return combine(
            repository.getMemberInfo(),
            repository.getRewardsProgress(),
        ) { memberInfo, progress ->
            ScanContent(
                memberInfo = memberInfo,
                rewardsProgress = progress,
            )
        }
    }
}
