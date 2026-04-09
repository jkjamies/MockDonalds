package com.mockdonalds.app.features.scan.data

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty

class ScanRepositoryImplTest : BehaviorSpec({

    Given("a scan repository implementation") {
        val repository = ScanRepositoryImpl()

        When("getting member info") {
            Then("it should emit valid member info") {
                repository.getMemberInfo().test {
                    val info = awaitItem()
                    info.memberStatus.shouldNotBeEmpty()
                    info.qrCodeUrl.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }

        When("getting rewards progress") {
            Then("it should emit valid progress data") {
                repository.getRewardsProgress().test {
                    val progress = awaitItem()
                    progress.currentPoints shouldBe 750
                    progress.message.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }
    }
})
