package com.mockdonalds.app.features.home.data

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty

class HomeRepositoryImplTest : BehaviorSpec({

    Given("a home repository implementation") {
        val repository = HomeRepositoryImpl()

        When("getting the user name") {
            Then("it should emit a non-empty name") {
                repository.getUserName().test {
                    awaitItem().shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }

        When("getting the hero promotion") {
            Then("it should emit a valid promotion") {
                repository.getHeroPromotion().test {
                    val promo = awaitItem()
                    promo.title.shouldNotBeEmpty()
                    promo.description.shouldNotBeEmpty()
                    promo.ctaText.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }

        When("getting recent cravings") {
            Then("it should emit a non-empty list") {
                repository.getRecentCravings().test {
                    val cravings = awaitItem()
                    cravings.size shouldBe 3
                    awaitComplete()
                }
            }
        }

        When("getting explore items") {
            Then("it should emit a non-empty list") {
                repository.getExploreItems().test {
                    val items = awaitItem()
                    items.size shouldBe 3
                    awaitComplete()
                }
            }
        }
    }
})
