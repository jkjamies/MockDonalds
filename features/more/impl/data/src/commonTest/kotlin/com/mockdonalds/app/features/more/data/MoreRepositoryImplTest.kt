package com.mockdonalds.app.features.more.data

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty

class MoreRepositoryImplTest : BehaviorSpec({

    Given("a more repository implementation") {
        val repository = MoreRepositoryImpl()

        When("getting user profile") {
            Then("it should emit a valid user profile") {
                repository.getUserProfile().test {
                    val profile = awaitItem()
                    profile.name.shouldNotBeEmpty()
                    profile.tier.shouldNotBeEmpty()
                    profile.points.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }

        When("getting menu items") {
            Then("it should emit a non-empty list") {
                repository.getMenuItems().test {
                    val items = awaitItem()
                    items.size shouldBe 5
                    items.first().title.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }
    }
})
