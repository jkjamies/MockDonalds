package com.mockdonalds.app.navigation

import com.mockdonalds.app.core.circuit.ProtectedScreen
import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.core.test.FakeAuthManager
import com.slack.circuit.runtime.screen.Screen
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

@Parcelize
private data object TestProtectedScreen : ProtectedScreen

@Parcelize
private data object TestPublicScreen : Screen

@Parcelize
private data class TestLoginScreen(val returnTo: Screen? = null) : Screen

class AuthInterceptorTest : BehaviorSpec({

    Given("an auth interceptor") {
        val authManager = FakeAuthManager()
        val interceptor = AuthInterceptor(authManager) { returnTo ->
            TestLoginScreen(returnTo = returnTo)
        }

        When("navigating to a protected screen while unauthenticated") {
            authManager.isAuthenticated = false
            val result = interceptor.interceptGoTo(TestProtectedScreen)

            Then("it should rewrite to login screen with returnTo") {
                result.shouldBeInstanceOf<InterceptResult.Rewrite>()
                val loginScreen = result.screen as TestLoginScreen
                loginScreen.returnTo shouldBe TestProtectedScreen
            }
        }

        When("navigating to a protected screen while authenticated") {
            authManager.isAuthenticated = true
            val result = interceptor.interceptGoTo(TestProtectedScreen)

            Then("it should skip") {
                result shouldBe InterceptResult.Skip
            }
        }

        When("navigating to a public screen while unauthenticated") {
            authManager.isAuthenticated = false
            val result = interceptor.interceptGoTo(TestPublicScreen)

            Then("it should skip") {
                result shouldBe InterceptResult.Skip
            }
        }
    }
})
