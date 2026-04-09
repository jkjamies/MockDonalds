package com.mockdonalds.app.features.home.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.core.circuit.TabScreen

@Parcelize
data object HomeScreen : TabScreen {
    override val tag: String = "home"
}
