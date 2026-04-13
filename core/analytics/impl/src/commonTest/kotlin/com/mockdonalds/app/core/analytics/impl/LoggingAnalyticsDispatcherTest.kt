package com.mockdonalds.app.core.analytics.impl

import com.mockdonalds.app.core.analytics.AnalyticsEvent
import io.kotest.core.spec.style.BehaviorSpec

private data class TestEvent(
    override val name: String = "test_event",
    override val properties: Map<String, Any> = emptyMap(),
) : AnalyticsEvent

class LoggingAnalyticsDispatcherTest : BehaviorSpec({

    Given("a logging analytics dispatcher") {
        val dispatcher = LoggingAnalyticsDispatcher()

        When("tracking an event") {
            Then("it should not throw") {
                dispatcher.track(TestEvent())
            }
        }

        When("tracking a screen view") {
            Then("it should not throw") {
                dispatcher.trackScreenView("HomeScreen")
            }
        }
    }
})
