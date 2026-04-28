package com.mockdonalds.app.navigation

import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.core.circuit.ProtectedScreen
import com.mockdonalds.app.core.logger.featureLogger
import com.slack.circuit.runtime.screen.Screen

private val log = featureLogger("Auth")

class AuthInterceptor(
    private val authManager: AuthManager,
    private val loginScreenFactory: (returnTo: Screen) -> Screen,
) : NavigationInterceptor {

    override fun interceptGoTo(screen: Screen): InterceptResult {
        if (screen is ProtectedScreen && !authManager.isAuthenticated) {
            log.i { "Auth-gated nav to $screen — redirecting to LoginScreen" }
            return InterceptResult.Rewrite(loginScreenFactory(screen))
        }
        return InterceptResult.Skip
    }
}
