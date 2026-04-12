package com.mockdonalds.app.features.login.api.navigation

import com.mockdonalds.app.core.circuit.FlowScreen
import com.mockdonalds.app.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data class LoginScreen(
    val returnTo: Screen? = null,
) : FlowScreen
