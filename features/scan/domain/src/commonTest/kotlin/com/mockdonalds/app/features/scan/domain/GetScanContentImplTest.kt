package com.mockdonalds.app.features.scan.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.scan.api.domain.MemberInfo
import com.mockdonalds.app.features.scan.api.domain.ScanContent
import com.mockdonalds.app.features.scan.api.domain.ScanRewardsProgress
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetScanContentImplTest : BehaviorSpec({

    Given("a scan content interactor with repository data") {
        val memberInfo = MutableStateFlow(
            MemberInfo(
                memberStatus = "Test Member",
                qrCodeUrl = "https://example.com/qr.png",
            ),
        )
        val rewardsProgress = MutableStateFlow(
            ScanRewardsProgress(
                currentPoints = 500,
                pointsToNextReward = 100,
                progressFraction = 0.83f,
                message = "Almost there!",
            ),
        )

        val repository = object : ScanRepository {
            override fun getMemberInfo(): Flow<MemberInfo> = memberInfo
            override fun getRewardsProgress(): Flow<ScanRewardsProgress> = rewardsProgress
        }

        val interactor = GetScanContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it should combine all repository data into ScanContent") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content shouldBe ScanContent(
                        memberInfo = memberInfo.value,
                        rewardsProgress = rewardsProgress.value,
                    )
                }
            }
        }

        When("the repository member info updates") {
            Then("it should emit updated content") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem()
                    memberInfo.value = memberInfo.value.copy(memberStatus = "Gold Member")
                    val updated = awaitItem()
                    updated.memberInfo.memberStatus shouldBe "Gold Member"
                }
            }
        }
    }
})
