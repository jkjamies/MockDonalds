package com.mockdonalds.app.features.kiosk.attract.data

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.string.shouldNotBeEmpty

class AttractRepositoryImplTest : BehaviorSpec({

    Given("the kiosk attract repository implementation") {
        val repository = AttractRepositoryImpl()

        When("getting attract content from the static fallback") {
            Then("it should emit a non-empty ad list with a positive rotation interval") {
                repository.getAttractContent().test {
                    val content = awaitItem()
                    content.ads.shouldNotBeEmpty()
                    content.rotationSeconds shouldBeGreaterThan 0
                    content.ads.first().headline.shouldNotBeEmpty()
                    content.ads.first().imageUrl.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }
    }
})
