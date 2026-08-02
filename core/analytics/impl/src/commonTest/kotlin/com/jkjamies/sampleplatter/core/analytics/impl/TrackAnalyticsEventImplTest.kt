package com.jkjamies.sampleplatter.core.analytics.impl

import com.jkjamies.sampleplatter.core.analytics.AnalyticsDispatcher
import com.jkjamies.sampleplatter.core.analytics.AnalyticsEvent
import com.jkjamies.sampleplatter.core.strata.StrataResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private data class TrackTestEvent(
    override val name: String = "test_event",
    override val properties: Map<String, Any> = emptyMap(),
) : AnalyticsEvent

class TrackAnalyticsEventImplTest : BehaviorSpec({

    Given("a track analytics event interactor") {
        val trackedEvents = mutableListOf<AnalyticsEvent>()

        val dispatcher = object : AnalyticsDispatcher {
            override fun track(event: AnalyticsEvent) {
                trackedEvents.add(event)
            }
            override fun trackScreenView(screenName: String) {}
        }

        val interactor = TrackAnalyticsEventImpl(dispatcher)

        When("invoked with an event") {
            val event = TrackTestEvent(name = "button_tapped")
            val result = interactor(event)

            Then("it should delegate to the analytics dispatcher") {
                trackedEvents shouldBe listOf(event)
            }

            Then("it should return success") {
                result.shouldBeInstanceOf<StrataResult.Success<Unit>>()
            }
        }
    }
})
