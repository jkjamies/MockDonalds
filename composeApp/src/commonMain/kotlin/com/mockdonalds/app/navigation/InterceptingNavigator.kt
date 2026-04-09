package com.mockdonalds.app.navigation

import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen

class InterceptingNavigator(
    private val delegate: Navigator,
    private val interceptors: List<NavigationInterceptor>,
) : Navigator by delegate {

    override fun goTo(screen: Screen): Boolean {
        val resolved = applyInterceptors(screen) { interceptGoTo(it) }
        return delegate.goTo(resolved)
    }

    override fun resetRoot(
        newRoot: Screen,
        options: Navigator.StateOptions,
    ): List<Screen> {
        val resolved = applyInterceptors(newRoot) { interceptResetRoot(it) }
        return delegate.resetRoot(resolved, options)
    }

    fun deepLink(screens: List<Screen>): List<Screen> {
        return screens.map { screen ->
            applyInterceptors(screen) { interceptGoTo(it) }
        }
    }

    private inline fun applyInterceptors(
        screen: Screen,
        intercept: NavigationInterceptor.(Screen) -> InterceptResult,
    ): Screen {
        var current = screen
        for (interceptor in interceptors) {
            when (val result = interceptor.intercept(current)) {
                is InterceptResult.Skip -> continue
                is InterceptResult.Rewrite -> current = result.screen
            }
        }
        return current
    }
}
