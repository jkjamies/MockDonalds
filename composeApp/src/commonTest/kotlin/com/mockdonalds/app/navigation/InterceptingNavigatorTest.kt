package com.mockdonalds.app.navigation

import com.mockdonalds.app.core.circuit.ProtectedScreen
import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.core.test.FakeAuthManager
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.test.FakeNavigator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

@Parcelize
private data object PublicScreen : Screen

@Parcelize
private data object AuthScreen : ProtectedScreen

@Parcelize
private data class LocalLoginScreen(val returnTo: Screen? = null) : Screen

class InterceptingNavigatorTest : BehaviorSpec({

    Given("an intercepting navigator with auth interceptor") {
        val authManager = FakeAuthManager()
        val delegate = FakeNavigator(PublicScreen)
        val navigator = InterceptingNavigator(
            delegate = delegate,
            interceptors = listOf(
                AuthInterceptor(authManager) { LocalLoginScreen(returnTo = it) },
            ),
        )

        When("goTo a public screen") {
            navigator.goTo(PublicScreen)
            Then("it should delegate unchanged") {
                delegate.awaitNextScreen() shouldBe PublicScreen
            }
        }

        When("goTo a protected screen while unauthenticated") {
            authManager.isAuthenticated = false
            val delegateForTest = FakeNavigator(PublicScreen)
            val nav = InterceptingNavigator(
                delegate = delegateForTest,
                interceptors = listOf(
                    AuthInterceptor(authManager) { LocalLoginScreen(returnTo = it) },
                ),
            )
            nav.goTo(AuthScreen)
            Then("it should rewrite to login") {
                val screen = delegateForTest.awaitNextScreen()
                screen shouldBe LocalLoginScreen(returnTo = AuthScreen)
            }
        }

        When("goTo a protected screen while authenticated") {
            authManager.isAuthenticated = true
            val delegateForTest = FakeNavigator(PublicScreen)
            val nav = InterceptingNavigator(
                delegate = delegateForTest,
                interceptors = listOf(
                    AuthInterceptor(authManager) { LocalLoginScreen(returnTo = it) },
                ),
            )
            nav.goTo(AuthScreen)
            Then("it should delegate unchanged") {
                delegateForTest.awaitNextScreen() shouldBe AuthScreen
            }
        }

        When("deepLink with mixed screens while unauthenticated") {
            authManager.isAuthenticated = false
            val nav = InterceptingNavigator(
                delegate = FakeNavigator(PublicScreen),
                interceptors = listOf(
                    AuthInterceptor(authManager) { LocalLoginScreen(returnTo = it) },
                ),
            )
            val result = nav.deepLink(listOf(PublicScreen, AuthScreen))
            Then("it should rewrite only protected screens") {
                result shouldBe listOf(PublicScreen, LocalLoginScreen(returnTo = AuthScreen))
            }
        }
    }
})
