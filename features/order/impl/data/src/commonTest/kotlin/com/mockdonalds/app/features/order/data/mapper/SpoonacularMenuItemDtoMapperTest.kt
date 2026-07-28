package com.mockdonalds.app.features.order.data.remote
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SpoonacularMenuItemDtoMapperTest : BehaviorSpec({

    Given("a Spoonacular DTO") {
        When("the dto has all fields populated") {
            Then("the mapper should produce a domain MenuItem with the categoryId attached") {
                val dto = SpoonacularMenuItemDto(
                    id = 12345,
                    title = "Big Mac",
                    restaurantChain = "Sample Platter",
                    image = "https://img/big-mac.jpg",
                    servingSize = "1 sandwich (214g)",
                )

                val item = dto.toMenuItem(categoryId = "burgers")

                item.id shouldBe "12345"
                item.title shouldBe "Big Mac"
                item.restaurantChain shouldBe "Sample Platter"
                item.imageUrl shouldBe "https://img/big-mac.jpg"
                item.servingSize shouldBe "1 sandwich (214g)"
                item.categoryId shouldBe "burgers"
            }
        }

        When("the dto has a null serving size") {
            Then("the mapper should preserve the null") {
                val dto = SpoonacularMenuItemDto(
                    id = 9,
                    title = "Diet Coke",
                    restaurantChain = "Generic",
                    image = "https://img/coke.jpg",
                    servingSize = null,
                )

                dto.toMenuItem(categoryId = "drinks").servingSize shouldBe null
            }
        }
    }
})
