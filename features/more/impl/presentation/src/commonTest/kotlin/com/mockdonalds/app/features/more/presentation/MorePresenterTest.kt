package com.mockdonalds.app.features.more.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.profile.api.navigation.ProfileScreen
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.mockdonalds.app.features.recents.api.navigation.RecentsScreen
import com.mockdonalds.app.features.more.test.FakeGetMoreContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class MorePresenterTest : BehaviorSpec({

    Given("a more presenter with content available") {
        val fakeGetMoreContent = FakeGetMoreContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(MoreScreen)

        When("the presenter emits state") {
            Then("it should start with empty defaults then populate") {
                presenterTestOf(
                    presentFunction = {
                        MorePresenter(
                            navigator = navigator,
                            getMoreContent = fakeGetMoreContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.userProfile shouldBe null

                    val state = awaitItem()
                    state.userProfile?.name shouldBe "Test User"
                    state.menuItems.size shouldBe 2
                }
            }
        }

        When("the user taps the profile") {
            Then("it should navigate to the profile screen") {
                presenterTestOf(
                    presentFunction = {
                        MorePresenter(
                            navigator = navigator,
                            getMoreContent = fakeGetMoreContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(MoreEvent.ProfileClicked)
                    navigator.awaitNextScreen() shouldBe ProfileScreen
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user taps the recents menu item") {
            Then("it should navigate to the recents screen") {
                presenterTestOf(
                    presentFunction = {
                        MorePresenter(
                            navigator = navigator,
                            getMoreContent = fakeGetMoreContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(MoreEvent.MenuItemClicked("1"))
                    navigator.awaitNextScreen() shouldBe RecentsScreen
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the content updates") {
            Then("the presenter should emit updated state") {
                presenterTestOf(
                    presentFunction = {
                        MorePresenter(
                            navigator = navigator,
                            getMoreContent = fakeGetMoreContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    skipItems(2)
                    fakeGetMoreContent.emit(
                        FakeGetMoreContent.DEFAULT.copy(
                            userProfile = FakeGetMoreContent.DEFAULT.userProfile.copy(name = "Updated User"),
                        ),
                    )
                    val updated = awaitItem()
                    updated.userProfile?.name shouldBe "Updated User"
                }
            }
        }
    }
})
