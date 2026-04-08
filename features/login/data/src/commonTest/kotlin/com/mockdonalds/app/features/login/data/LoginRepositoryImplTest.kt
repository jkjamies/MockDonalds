package com.mockdonalds.app.features.login.data

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldNotBeEmpty

class LoginRepositoryImplTest : BehaviorSpec({

    Given("a login repository implementation") {
        val repository = LoginRepositoryImpl()

        When("getting login content") {
            Then("it should emit content with a non-empty logo URL") {
                repository.getLoginContent().test {
                    val content = awaitItem()
                    content.logoUrl.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }
    }
})
