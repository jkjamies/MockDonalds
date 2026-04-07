package com.mockdonalds.app.features.rewards.data

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty

class RewardsRepositoryImplTest : BehaviorSpec({

    Given("a rewards repository implementation") {
        val repository = RewardsRepositoryImpl()

        When("getting rewards progress") {
            Then("it should emit valid progress data") {
                repository.getRewardsProgress().test {
                    val progress = awaitItem()
                    progress.currentPoints shouldBe 5432
                    progress.nextRewardName.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }

        When("getting vault specials") {
            Then("it should emit a non-empty list") {
                repository.getVaultSpecials().test {
                    val specials = awaitItem()
                    specials.size shouldBe 3
                    specials.first().title.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }

        When("getting history") {
            Then("it should emit a non-empty list with valid entries") {
                repository.getHistory().test {
                    val history = awaitItem()
                    history.size shouldBe 3
                    history.first().title.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }
    }
})
