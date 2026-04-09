package com.mockdonalds.app.features.scan.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.scan.api.navigation.ScanScreen
import com.mockdonalds.app.features.scan.test.FakeGetScanContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class ScanPresenterTest : BehaviorSpec({

    Given("a scan presenter with content available") {
        val fakeGetScanContent = FakeGetScanContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(ScanScreen)

        When("the presenter emits state") {
            Then("it should start with empty defaults then populate") {
                presenterTestOf(
                    presentFunction = {
                        ScanPresenter(
                            navigator = navigator,
                            getScanContent = fakeGetScanContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.memberInfo shouldBe null

                    val state = awaitItem()
                    state.memberInfo?.memberStatus shouldBe "Test Member"
                    state.rewardsProgress?.currentPoints shouldBe 500
                }
            }
        }

        When("the content updates") {
            Then("the presenter should emit updated state") {
                presenterTestOf(
                    presentFunction = {
                        ScanPresenter(
                            navigator = navigator,
                            getScanContent = fakeGetScanContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(2)
                    fakeGetScanContent.emit(
                        FakeGetScanContent.DEFAULT.copy(
                            memberInfo = FakeGetScanContent.DEFAULT.memberInfo.copy(memberStatus = "Gold Member"),
                        ),
                    )
                    val updated = awaitItem()
                    updated.memberInfo?.memberStatus shouldBe "Gold Member"
                }
            }
        }
    }
})
