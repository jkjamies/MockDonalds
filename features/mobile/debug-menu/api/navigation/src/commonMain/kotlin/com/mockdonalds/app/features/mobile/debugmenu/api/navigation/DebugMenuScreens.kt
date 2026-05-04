package com.mockdonalds.app.features.mobile.debugmenu.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data object DebugMenuScreen : Screen

@Parcelize
data object FeatureFlagsDebugScreen : Screen

@Parcelize
data object BuildConfigDebugScreen : Screen
