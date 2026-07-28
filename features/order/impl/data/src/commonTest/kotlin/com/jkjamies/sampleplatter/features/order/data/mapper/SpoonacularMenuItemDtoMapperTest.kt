package com.jkjamies.sampleplatter.features.order.data.remote
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SpoonacularMenuItemDtoMapperTest : BehaviorSpec({

    Given("a Spoonacular DTO") {
        When("the dto has all fields populated") {
            Then("the mapper should produce a domain MenuItem with the categoryId attached") {
                val dto = SpoonacularMenuItemDto(
                    id = 12345,
                    title = "Signature Stack",
                    restaurantChain = "Sample Platter",
                    image = "https://img/big-mac.jpg",
                    servingSize = "1 sandwich (214g)",
                )

                val item = dto.toMenuItem(categoryId = "burgers")

                item.id shouldBe "12345"
                item.title shouldBe "Signature Stack"
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
                    title = "Diet Cola",
                    restaurantChain = "Generic",
                    image = "https://img/diet-cola.jpg",
                    servingSize = null,
                )

                dto.toMenuItem(categoryId = "drinks").servingSize shouldBe null
            }
        }
    }
})
