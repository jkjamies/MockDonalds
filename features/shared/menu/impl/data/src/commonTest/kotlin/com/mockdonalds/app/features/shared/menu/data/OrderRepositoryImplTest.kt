package com.mockdonalds.app.features.shared.menu.data

import app.cash.turbine.test
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotBeEmpty

class OrderRepositoryImplTest : BehaviorSpec({

    Given("an order repository implementation") {
        val repository = OrderRepositoryImpl()

        When("getting menu categories") {
            Then("it should emit a non-empty list") {
                repository.getMenuCategories().test {
                    val categories = awaitItem()
                    categories.shouldNotBeEmpty()
                    categories.first().name.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }

        When("getting featured items") {
            Then("it should emit a non-empty list with valid data") {
                repository.getFeaturedItems().test {
                    val items = awaitItem()
                    items.size shouldBe 2
                    items.first().title.shouldNotBeEmpty()
                    items.first().price.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }

        When("getting items by category") {
            Then("every category id maps to at least one item with non-empty fields") {
                repository.getItemsByCategory().test {
                    val map = awaitItem()
                    map.shouldNotBeEmpty()
                    map.values.forEach { items ->
                        items.shouldNotBeEmpty()
                        items.first().name.shouldNotBeEmpty()
                        items.first().priceFormatted.shouldNotBeEmpty()
                    }
                    awaitComplete()
                }
            }
        }

        When("getting cart summary") {
            Then("it should emit a valid cart summary") {
                repository.getCartSummary().test {
                    val cart = awaitItem()
                    cart.itemCount shouldBe 2
                    cart.total.shouldNotBeEmpty()
                    awaitComplete()
                }
            }
        }
    }
})
