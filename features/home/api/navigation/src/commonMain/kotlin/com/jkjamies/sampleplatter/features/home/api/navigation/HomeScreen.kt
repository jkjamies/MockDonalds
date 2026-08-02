package com.jkjamies.sampleplatter.features.home.api.navigation

import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.jkjamies.sampleplatter.core.circuit.TabScreen

@Parcelize
data object HomeScreen : TabScreen {
    override val tag: String = "home"
}
