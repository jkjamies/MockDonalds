package com.mockdonalds.app.features.recents.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.recents.api.domain.RecentsContent
import com.mockdonalds.app.features.recents.api.navigation.RecentsScreen
import com.mockdonalds.app.features.recents.test.FakeGetRecentsContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class RecentsPresenterTest : BehaviorSpec({

    Given("a recents presenter with content available") {
        val fakeContent = FakeGetRecentsContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(RecentsScreen)

        When("the presenter emits state") {
            Then("it should start loading then show content") {
                presenterTestOf(
                    presentFunction = {
                        RecentsPresenter(
                            navigator = navigator,
                            getRecentsContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.shouldBeInstanceOf<RecentsUiState.Loading>()

                    val state = awaitItem()
                    state.shouldBeInstanceOf<RecentsUiState.Success>()
                    state.items.size shouldBe 5
                }
            }
        }

        When("the user taps back") {
            Then("it should pop the navigator") {
                presenterTestOf(
                    presentFunction = {
                        RecentsPresenter(
                            navigator = navigator,
                            getRecentsContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(RecentsEvent.OnBackTapped)
                    navigator.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the content updates") {
            Then("the presenter should emit updated state") {
                presenterTestOf(
                    presentFunction = {
                        RecentsPresenter(
                            navigator = navigator,
                            getRecentsContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(2)
                    fakeContent.emit(RecentsContent(emptyList()))
                    val updated = awaitItem()
                    updated.shouldBeInstanceOf<RecentsUiState.Empty>()
                }
            }
        }
    }

    Given("a recents presenter with no content available") {
        val fakeContent = FakeGetRecentsContent(initial = RecentsContent(emptyList()))
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(RecentsScreen)

        When("the presenter emits state") {
            Then("it should start loading then show empty") {
                presenterTestOf(
                    presentFunction = {
                        RecentsPresenter(
                            navigator = navigator,
                            getRecentsContent = fakeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.shouldBeInstanceOf<RecentsUiState.Loading>()

                    val state = awaitItem()
                    state.shouldBeInstanceOf<RecentsUiState.Empty>()
                }
            }
        }
    }
})
