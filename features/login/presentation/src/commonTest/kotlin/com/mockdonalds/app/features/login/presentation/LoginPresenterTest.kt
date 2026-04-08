package com.mockdonalds.app.features.login.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.login.api.navigation.LoginScreen
import com.mockdonalds.app.features.login.test.FakeGetLoginContent
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class LoginPresenterTest : BehaviorSpec({

    Given("a login presenter") {
        val fakeGetLoginContent = FakeGetLoginContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(LoginScreen)

        When("the presenter emits initial state") {
            Then("it should have empty fields") {
                presenterTestOf(
                    presentFunction = {
                        LoginPresenter(
                            navigator = navigator,
                            getLoginContent = fakeGetLoginContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.email shouldBe ""
                    initial.password shouldBe ""
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
                            navigator = navigator,
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

        When("the user types a password") {
            Then("the password state should update") {
                presenterTestOf(
                    presentFunction = {
                        LoginPresenter(
                            navigator = navigator,
                            getLoginContent = fakeGetLoginContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.eventSink(LoginEvent.PasswordChanged("secret123"))
                    val updated = awaitItem()
                    updated.password shouldBe "secret123"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
