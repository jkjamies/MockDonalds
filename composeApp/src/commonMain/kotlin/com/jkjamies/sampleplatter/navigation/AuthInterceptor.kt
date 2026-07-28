package com.jkjamies.sampleplatter.navigation

import com.jkjamies.sampleplatter.core.auth.AuthManager
import com.jkjamies.sampleplatter.core.circuit.ProtectedScreen
import com.jkjamies.sampleplatter.core.logger.featureLogger
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
