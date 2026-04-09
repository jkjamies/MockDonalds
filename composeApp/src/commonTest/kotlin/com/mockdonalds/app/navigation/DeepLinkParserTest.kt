package com.mockdonalds.app.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

@Parcelize
private data object HomeScreen : Screen

@Parcelize
private data object ProfileScreen : Screen

class DeepLinkParserTest : BehaviorSpec({

    val parser = DeepLinkParser(
        screenRegistry = mapOf(
            "home" to { HomeScreen },
            "profile" to { ProfileScreen },
        ),
    )

    Given("a deep link parser") {

        When("parsing a valid single-segment URI with a tab root") {
            val result = parser.parse("mockdonalds://app/home")
            Then("it should return the matching screen") {
                result shouldBe listOf(HomeScreen)
            }
        }

        When("parsing a multi-segment URI with tab root and nested screen") {
            val result = parser.parse("mockdonalds://app/home/profile")
            Then("it should return screens in order") {
                result shouldBe listOf(HomeScreen, ProfileScreen)
            }
        }

        When("parsing a URI with unknown segments") {
            val result = parser.parse("mockdonalds://app/home/unknown/profile")
            Then("it should skip unknown segments") {
                result shouldBe listOf(HomeScreen, ProfileScreen)
            }
        }

        When("parsing a URI with no matching segments") {
            val result = parser.parse("mockdonalds://app/unknown")
            Then("it should return null") {
                result shouldBe null
            }
        }

        When("parsing an empty path") {
            val result = parser.parse("mockdonalds://app/")
            Then("it should return null") {
                result shouldBe null
            }
        }

        When("parsing an invalid URI") {
            val result = parser.parse("")
            Then("it should return null") {
                result shouldBe null
            }
        }
    }
})
