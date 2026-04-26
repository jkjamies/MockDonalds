package com.mockdonalds.app.features.debugmenu.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.mockdonalds.app.features.debugmenu.api.navigation.DebugMenuScreen
import com.mockdonalds.app.features.debugmenu.api.navigation.FeatureFlagsDebugScreen
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class DebugMenuPresenterTest : BehaviorSpec({

    Given("a debug menu presenter") {
        val dispatchers = TestCenterPostDispatchers()

        When("the presenter emits state") {
            Then("it should expose the feature-flags and build-config entries") {
                val navigator = FakeNavigator(DebugMenuScreen, DebugMenuScreen)
                presenterTestOf(
                    presentFunction = {
                        DebugMenuPresenter(navigator = navigator, dispatchers = dispatchers)
                    },
                ) {
                    val state = awaitItem()
                    state.entries.map { it.id } shouldBe listOf("feature-flags", "build-config")
                }
            }
        }

        When("the user taps the feature-flags entry") {
            Then("it should navigate to FeatureFlagsDebugScreen") {
                val navigator = FakeNavigator(DebugMenuScreen, DebugMenuScreen)
                presenterTestOf(
                    presentFunction = {
                        DebugMenuPresenter(navigator = navigator, dispatchers = dispatchers)
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(DebugMenuEvent.EntryClicked("feature-flags"))
                    navigator.awaitNextScreen() shouldBe FeatureFlagsDebugScreen
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user taps the build-config entry") {
            Then("it should navigate to BuildConfigDebugScreen") {
                val navigator = FakeNavigator(DebugMenuScreen, DebugMenuScreen)
                presenterTestOf(
                    presentFunction = {
                        DebugMenuPresenter(navigator = navigator, dispatchers = dispatchers)
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(DebugMenuEvent.EntryClicked("build-config"))
                    navigator.awaitNextScreen() shouldBe BuildConfigDebugScreen
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user taps an unknown entry") {
            Then("it should not navigate anywhere") {
                val navigator = FakeNavigator(DebugMenuScreen, DebugMenuScreen)
                presenterTestOf(
                    presentFunction = {
                        DebugMenuPresenter(navigator = navigator, dispatchers = dispatchers)
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(DebugMenuEvent.EntryClicked("unknown"))
                    navigator.expectNoGoToEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user taps back") {
            Then("it should pop the navigator") {
                val navigator = FakeNavigator(DebugMenuScreen)
                presenterTestOf(
                    presentFunction = {
                        DebugMenuPresenter(navigator = navigator, dispatchers = dispatchers)
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(DebugMenuEvent.BackClicked)
                    navigator.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
