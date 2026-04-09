package com.mockdonalds.app.features.home.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.mockdonalds.app.features.home.test.FakeGetHomeContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class HomePresenterTest : BehaviorSpec({

    Given("a home presenter with content available") {
        val fakeGetHomeContent = FakeGetHomeContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(HomeScreen)

        When("the presenter emits state") {
            Then("it should start with empty defaults then populate") {
                presenterTestOf(
                    presentFunction = {
                        HomePresenter(
                            navigator = navigator,
                            getHomeContent = fakeGetHomeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.userName shouldBe ""

                    val state = awaitItem()
                    state.userName shouldBe "TestUser"
                    state.heroPromotion?.title shouldBe "Test Promo"
                    state.recentCravings.size shouldBe 1
                    state.exploreItems.size shouldBe 1
                }
            }
        }

        When("the content updates") {
            Then("the presenter should emit updated state") {
                presenterTestOf(
                    presentFunction = {
                        HomePresenter(
                            navigator = navigator,
                            getHomeContent = fakeGetHomeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(2)
                    fakeGetHomeContent.emit(
                        FakeGetHomeContent.DEFAULT.copy(userName = "UpdatedUser"),
                    )
                    val updated = awaitItem()
                    updated.userName shouldBe "UpdatedUser"
                }
            }
        }
    }
})
