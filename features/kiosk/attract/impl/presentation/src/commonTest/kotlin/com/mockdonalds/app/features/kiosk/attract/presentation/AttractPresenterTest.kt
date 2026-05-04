package com.mockdonalds.app.features.kiosk.attract.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.kiosk.attract.api.navigation.AttractScreen
import com.mockdonalds.app.features.kiosk.attract.test.FakeGetAttractContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe

class AttractPresenterTest : BehaviorSpec({

    Given("a kiosk attract presenter with content available") {
        val fakeContent = FakeGetAttractContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(AttractScreen)

        When("the presenter emits state") {
            Then("it should publish the default ads with currentIndex=0") {
                presenterTestOf(
                    presentFunction = {
                        AttractPresenter(
                            navigator = navigator,
                            getAttractContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    awaitItem() // initial empty
                    val state = awaitItem()
                    state.ads.shouldNotBeEmpty()
                    state.currentIndex shouldBe 0
                    state.rotationSeconds shouldBe FakeGetAttractContent.DEFAULT.rotationSeconds
                }
            }
        }

        When("the IndexChanged event is dispatched") {
            Then("the presenter advances currentIndex") {
                presenterTestOf(
                    presentFunction = {
                        AttractPresenter(
                            navigator = navigator,
                            getAttractContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(1)
                    val state = awaitItem()
                    state.eventSink(AttractEvent.IndexChanged(2))
                    val advanced = awaitItem()
                    advanced.currentIndex shouldBe 2
                }
            }
        }
    }
})
