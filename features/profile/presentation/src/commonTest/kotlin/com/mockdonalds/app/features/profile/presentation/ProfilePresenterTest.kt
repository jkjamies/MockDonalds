package com.mockdonalds.app.features.profile.presentation

import com.mockdonalds.app.core.test.FakeAuthManager
import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.profile.api.navigation.ProfileScreen
import com.mockdonalds.app.features.profile.test.FakeGetProfileContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class ProfilePresenterTest : BehaviorSpec({

    Given("a profile presenter") {
        val fakeGetProfileContent = FakeGetProfileContent()
        val dispatchers = TestCenterPostDispatchers()
        val authManager = FakeAuthManager(isAuthenticated = true)
        val navigator = FakeNavigator(ProfileScreen)

        When("the presenter emits state") {
            Then("it should populate with profile content") {
                presenterTestOf(
                    presentFunction = {
                        ProfilePresenter(
                            navigator = navigator,
                            authManager = authManager,
                            getProfileContent = fakeGetProfileContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.name shouldBe ""

                    val state = awaitItem()
                    state.name shouldBe "Night Owl"
                    state.email shouldBe "gourmet@night.com"
                    state.tier shouldBe "Gold"
                    state.points shouldBe "4,280 pts"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user taps logout") {
            Then("it should log out and pop") {
                val navForTest = FakeNavigator(ProfileScreen)
                val authForTest = FakeAuthManager(isAuthenticated = true)
                presenterTestOf(
                    presentFunction = {
                        ProfilePresenter(
                            navigator = navForTest,
                            authManager = authForTest,
                            getProfileContent = fakeGetProfileContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    awaitItem() // initial
                    val state = awaitItem() // populated
                    state.eventSink(ProfileEvent.LogoutClicked)
                    authForTest.isAuthenticated shouldBe false
                    navForTest.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
