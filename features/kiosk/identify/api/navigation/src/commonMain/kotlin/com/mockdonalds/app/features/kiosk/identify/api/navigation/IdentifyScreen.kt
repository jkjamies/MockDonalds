package com.mockdonalds.app.features.kiosk.identify.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data class IdentifyScreen(
    val next: Screen? = null,
) : Screen
