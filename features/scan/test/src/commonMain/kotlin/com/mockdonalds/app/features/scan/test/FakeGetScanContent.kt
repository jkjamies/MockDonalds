package com.mockdonalds.app.features.scan.test

import com.mockdonalds.app.features.scan.api.domain.GetScanContent
import com.mockdonalds.app.features.scan.api.domain.MemberInfo
import com.mockdonalds.app.features.scan.api.domain.ScanContent
import com.mockdonalds.app.features.scan.api.domain.ScanRewardsProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetScanContent(
    initial: ScanContent = DEFAULT,
) : GetScanContent() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<ScanContent> = _content

    fun emit(content: ScanContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = ScanContent(
            memberInfo = MemberInfo(
                memberStatus = "Test Member",
                qrCodeUrl = "",
            ),
            rewardsProgress = ScanRewardsProgress(
                currentPoints = 500,
                pointsToNextReward = 100,
                progressFraction = 0.83f,
                message = "Almost there!",
            ),
        )
    }
}
