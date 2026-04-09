package com.mockdonalds.app.features.rewards.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.mockdonalds.app.features.rewards.test.FakeGetRewardsContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RewardsPresenterTest : BehaviorSpec({

    Given("a rewards presenter with content available") {
        val fakeGetRewardsContent = FakeGetRewardsContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(RewardsScreen)

        When("the presenter emits state") {
            Then("it should start with empty defaults then populate") {
                presenterTestOf(
                    presentFunction = {
                        RewardsPresenter(
                            navigator = navigator,
                            getRewardsContent = fakeGetRewardsContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.progress shouldBe null

                    val state = awaitItem()
                    state.progress?.currentPoints shouldBe 1000
                    state.vaultSpecials.size shouldBe 1
                    state.history.size shouldBe 1
                }
            }
        }

        When("the content updates") {
            Then("the presenter should emit updated state") {
                presenterTestOf(
                    presentFunction = {
                        RewardsPresenter(
                            navigator = navigator,
                            getRewardsContent = fakeGetRewardsContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(2)
                    fakeGetRewardsContent.emit(
                        FakeGetRewardsContent.DEFAULT.copy(
                            progress = FakeGetRewardsContent.DEFAULT.progress.copy(currentPoints = 5000),
                        ),
                    )
                    val updated = awaitItem()
                    updated.progress?.currentPoints shouldBe 5000
                }
            }
        }
    }
})
