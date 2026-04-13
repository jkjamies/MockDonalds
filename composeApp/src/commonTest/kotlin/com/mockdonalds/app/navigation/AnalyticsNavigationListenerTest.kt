package com.mockdonalds.app.navigation

import com.mockdonalds.app.core.analytics.test.FakeAnalyticsDispatcher
import com.mockdonalds.app.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

@Parcelize
private data object TestScreen : Screen

class AnalyticsNavigationListenerTest : BehaviorSpec({

    Given("an analytics navigation listener") {
        val fakeDispatcher = FakeAnalyticsDispatcher()
        val listener = AnalyticsNavigationListener(fakeDispatcher)

        When("onGoTo is called") {
            listener.onGoTo(TestScreen)

            Then("it should track the screen view with the screen class name") {
                fakeDispatcher.trackedScreenViews shouldBe listOf("TestScreen")
            }
        }

        When("onResetRoot is called") {
            fakeDispatcher.reset()
            listener.onResetRoot(TestScreen)

            Then("it should track the screen view with the screen class name") {
                fakeDispatcher.trackedScreenViews shouldBe listOf("TestScreen")
            }
        }

    }
})
