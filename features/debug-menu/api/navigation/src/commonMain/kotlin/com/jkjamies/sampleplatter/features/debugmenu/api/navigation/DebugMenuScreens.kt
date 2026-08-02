package com.jkjamies.sampleplatter.features.debugmenu.api.navigation

import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data object DebugMenuScreen : Screen

@Parcelize
data object FeatureFlagsDebugScreen : Screen

@Parcelize
data object BuildConfigDebugScreen : Screen
