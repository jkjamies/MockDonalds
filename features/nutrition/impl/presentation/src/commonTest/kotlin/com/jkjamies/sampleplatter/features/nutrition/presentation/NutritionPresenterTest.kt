package com.jkjamies.sampleplatter.features.nutrition.presentation

import com.jkjamies.sampleplatter.core.test.TestCenterPostDispatchers
import com.jkjamies.sampleplatter.features.nutrition.api.domain.NutritionContent
import com.jkjamies.sampleplatter.features.nutrition.api.navigation.NutritionScreen
import com.jkjamies.sampleplatter.features.nutrition.test.FakeGetNutritionContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class NutritionPresenterTest : BehaviorSpec({

    Given("a nutrition presenter with a configured URL") {
        val sentinelUrl = "https://example.test/nutrition"
        val fakeContent = FakeGetNutritionContent(NutritionContent(url = sentinelUrl))
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(NutritionScreen)

        When("the presenter emits state") {
            Then("the URL flows through to UiState") {
                presenterTestOf(
                    presentFunction = {
                        NutritionPresenter(
                            navigator = navigator,
                            getNutritionContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.url shouldBe null

                    val state = awaitItem()
                    state.url shouldBe sentinelUrl
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user dispatches BackClicked") {
            Then("the navigator pops") {
                presenterTestOf(
                    presentFunction = {
                        NutritionPresenter(
                            navigator = navigator,
                            getNutritionContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(NutritionEvent.BackClicked)
                    navigator.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the content updates with a new URL") {
            Then("the presenter emits the updated URL") {
                presenterTestOf(
                    presentFunction = {
                        NutritionPresenter(
                            navigator = navigator,
                            getNutritionContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(2)
                    val updatedUrl = "https://example.test/nutrition/updated"
                    fakeContent.emit(NutritionContent(url = updatedUrl))
                    val updated = awaitItem()
                    updated.url shouldBe updatedUrl
                }
            }
        }
    }
})
