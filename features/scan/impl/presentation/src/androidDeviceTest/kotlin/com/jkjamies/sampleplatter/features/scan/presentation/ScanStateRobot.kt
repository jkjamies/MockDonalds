package com.jkjamies.sampleplatter.features.scan.presentation

import com.jkjamies.sampleplatter.core.test.StateRobot
import com.jkjamies.sampleplatter.features.scan.api.domain.MemberInfo
import com.jkjamies.sampleplatter.features.scan.api.domain.ScanRewardsProgress

class ScanStateRobot : StateRobot<ScanUiState, ScanEvent>() {

    override fun defaultState() = ScanUiState(
        memberInfo = MemberInfo(
            memberStatus = "Gold Member",
            qrCodeUrl = "",
        ),
        rewardsProgress = ScanRewardsProgress(
            currentPoints = 1250,
            pointsToNextReward = 250,
            progressFraction = 0.83f,
            message = "250 points to your next reward!",
        ),
        eventSink = createEventSink(),
    )

    fun stateWithNoMember() = defaultState().copy(
        memberInfo = null,
        eventSink = createEventSink(),
    )

    fun stateWithNoProgress() = defaultState().copy(
        rewardsProgress = null,
        eventSink = createEventSink(),
    )
}
