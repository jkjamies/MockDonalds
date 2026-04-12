package com.mockdonalds.app.features.login.presentation

import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.features.login.api.navigation.WelcomeScreen
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

@Parcelize
private data object WelcomeReturnToScreen : Screen

class WelcomePresenterTest : BehaviorSpec({

    Given("a welcome presenter") {

        When("the presenter emits initial state") {
            Then("it should have an eventSink") {
                val navigator = FakeNavigator(WelcomeScreen())
                presenterTestOf(
                    presentFunction = {
                        WelcomePresenter(
                            screen = WelcomeScreen(),
                            navigator = navigator,
                        )
                    },
                ) {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user clicks continue without returnTo") {
            Then("it should pop twice without navigating") {
                val navigator = FakeNavigator(WelcomeScreen())
                presenterTestOf(
                    presentFunction = {
                        WelcomePresenter(
                            screen = WelcomeScreen(),
                            navigator = navigator,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(WelcomeEvent.ContinueClicked)
                    navigator.awaitPop()
                    navigator.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user clicks continue with returnTo") {
            Then("it should pop twice and navigate to returnTo") {
                val navigator = FakeNavigator(WelcomeScreen(returnTo = WelcomeReturnToScreen))
                presenterTestOf(
                    presentFunction = {
                        WelcomePresenter(
                            screen = WelcomeScreen(returnTo = WelcomeReturnToScreen),
                            navigator = navigator,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(WelcomeEvent.ContinueClicked)
                    navigator.awaitPop()
                    navigator.awaitPop()
                    navigator.awaitNextScreen() shouldBe WelcomeReturnToScreen
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
