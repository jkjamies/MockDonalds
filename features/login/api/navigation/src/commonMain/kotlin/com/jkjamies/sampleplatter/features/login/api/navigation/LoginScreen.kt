package com.jkjamies.sampleplatter.features.login.api.navigation

import com.jkjamies.sampleplatter.core.circuit.FlowScreen
import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data class LoginScreen(
    val returnTo: Screen? = null,
) : FlowScreen
