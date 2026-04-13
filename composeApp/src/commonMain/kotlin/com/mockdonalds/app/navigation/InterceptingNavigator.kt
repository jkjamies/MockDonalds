package com.mockdonalds.app.navigation

import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen

class InterceptingNavigator(
    private val delegate: Navigator,
    private val interceptors: List<NavigationInterceptor>,
    private val listeners: List<NavigationEventListener> = emptyList(),
) : Navigator by delegate {

    override fun goTo(screen: Screen): Boolean {
        val resolved = applyInterceptors(screen) { interceptGoTo(it) }
        val success = delegate.goTo(resolved)
        if (success) {
            listeners.forEach { it.onGoTo(resolved) }
        }
        return success
    }

    override fun resetRoot(
        newRoot: Screen,
        options: Navigator.StateOptions,
    ): List<Screen> {
        val resolved = applyInterceptors(newRoot) { interceptResetRoot(it) }
        val popped = delegate.resetRoot(resolved, options)
        listeners.forEach { it.onResetRoot(resolved) }
        return popped
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
