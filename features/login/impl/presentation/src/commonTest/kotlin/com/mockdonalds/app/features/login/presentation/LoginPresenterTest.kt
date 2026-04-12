package com.mockdonalds.app.features.login.presentation

import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.core.test.FakeAuthManager
import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.login.api.navigation.LoginScreen
import com.mockdonalds.app.features.login.test.FakeGetLoginContent
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

@Parcelize
private data object ReturnToScreen : Screen

class LoginPresenterTest : BehaviorSpec({

    Given("a login presenter") {
        val fakeGetLoginContent = FakeGetLoginContent()
        val dispatchers = TestCenterPostDispatchers()
        val authManager = FakeAuthManager()
        val navigator = FakeNavigator(LoginScreen())

        When("the presenter emits initial state") {
            Then("it should have empty fields") {
                presenterTestOf(
                    presentFunction = {
                        LoginPresenter(
                            screen = LoginScreen(),
                            navigator = navigator,
                            authManager = authManager,
                            getLoginContent = fakeGetLoginContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.email shouldBe ""
                    initial.isLoading shouldBe false
                    initial.errorMessage shouldBe null
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user types an email") {
            Then("the email state should update") {
                presenterTestOf(
                    presentFunction = {
                        LoginPresenter(
                            screen = LoginScreen(),
                            navigator = navigator,
                            authManager = authManager,
                            getLoginContent = fakeGetLoginContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.eventSink(LoginEvent.EmailChanged("test@example.com"))
                    val updated = awaitItem()
                    updated.email shouldBe "test@example.com"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user confirms sign in without returnTo") {
            Then("it should log in and pop") {
                val navForTest = FakeNavigator(LoginScreen())
                val authForTest = FakeAuthManager()
                presenterTestOf(
                    presentFunction = {
                        LoginPresenter(
                            screen = LoginScreen(),
                            navigator = navForTest,
                            authManager = authForTest,
                            getLoginContent = fakeGetLoginContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(LoginEvent.SignInConfirmed)
                    authForTest.isAuthenticated shouldBe true
                    navForTest.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user confirms sign in with returnTo") {
            Then("it should log in, pop, and navigate to returnTo") {
                val navForTest = FakeNavigator(LoginScreen(returnTo = ReturnToScreen))
                val authForTest = FakeAuthManager()
                presenterTestOf(
                    presentFunction = {
                        LoginPresenter(
                            screen = LoginScreen(returnTo = ReturnToScreen),
                            navigator = navForTest,
                            authManager = authForTest,
                            getLoginContent = fakeGetLoginContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(LoginEvent.SignInConfirmed)
                    authForTest.isAuthenticated shouldBe true
                    navForTest.awaitPop()
                    navForTest.awaitNextScreen() shouldBe ReturnToScreen
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
        When("the user clicks dismiss") {
            Then("it should pop without logging in") {
                val navForTest = FakeNavigator(LoginScreen())
                val authForTest = FakeAuthManager()
                presenterTestOf(
                    presentFunction = {
                        LoginPresenter(
                            screen = LoginScreen(),
                            navigator = navForTest,
                            authManager = authForTest,
                            getLoginContent = fakeGetLoginContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(LoginEvent.DismissClicked)
                    authForTest.isAuthenticated shouldBe false
                    navForTest.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
