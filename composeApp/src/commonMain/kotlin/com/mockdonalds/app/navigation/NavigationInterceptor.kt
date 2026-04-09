package com.mockdonalds.app.navigation

import com.slack.circuit.runtime.screen.Screen

sealed class InterceptResult {
    data object Skip : InterceptResult()
    data class Rewrite(val screen: Screen) : InterceptResult()
}

interface NavigationInterceptor {
    fun interceptGoTo(screen: Screen): InterceptResult = InterceptResult.Skip
    fun interceptResetRoot(screen: Screen): InterceptResult = InterceptResult.Skip
}
