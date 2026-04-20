package com.mockdonalds.app.features.debugmenu.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec

class BuildConfigDebugPresenterTest : BehaviorSpec({

    Given("a build config debug presenter") {
        val dispatchers = TestCenterPostDispatchers()

        When("the presenter emits state") {
            Then("it should expose an event sink") {
                val navigator = FakeNavigator(BuildConfigDebugScreen, BuildConfigDebugScreen)
                presenterTestOf(
                    presentFunction = {
                        BuildConfigDebugPresenter(navigator = navigator, dispatchers = dispatchers)
                    },
                ) {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user taps back") {
            Then("it should pop the navigator") {
                val navigator = FakeNavigator(BuildConfigDebugScreen)
                presenterTestOf(
                    presentFunction = {
                        BuildConfigDebugPresenter(navigator = navigator, dispatchers = dispatchers)
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(BuildConfigDebugEvent.BackClicked)
                    navigator.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
