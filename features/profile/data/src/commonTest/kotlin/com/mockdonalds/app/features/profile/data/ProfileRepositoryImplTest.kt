package com.mockdonalds.app.features.profile.data

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldNotBeEmpty

class ProfileRepositoryImplTest : BehaviorSpec({

    Given("a profile repository implementation") {
        val repository = ProfileRepositoryImpl()

        When("getting the profile") {
            Then("it should emit a valid profile content") {
                repository.getProfile().test {
                    val profile = awaitItem()
                    profile.name.shouldNotBeEmpty()
                    profile.email.shouldNotBeEmpty()
                    profile.tier.shouldNotBeEmpty()
                    profile.points.shouldNotBeEmpty()
                    profile.memberSince.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }
    }
})
