package com.mockdonalds.app.navigation

import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.core.circuit.ProtectedScreen
import com.slack.circuit.runtime.screen.Screen

class AuthInterceptor(
    private val authManager: AuthManager,
    private val loginScreenFactory: (returnTo: Screen) -> Screen,
) : NavigationInterceptor {

    override fun interceptGoTo(screen: Screen): InterceptResult {
        if (screen is ProtectedScreen && !authManager.isAuthenticated) {
            return InterceptResult.Rewrite(loginScreenFactory(screen))
        }
        return InterceptResult.Skip
    }
}
