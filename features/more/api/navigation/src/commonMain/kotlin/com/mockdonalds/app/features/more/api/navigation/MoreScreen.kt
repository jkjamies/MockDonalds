package com.mockdonalds.app.features.more.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.core.circuit.TabScreen

@Parcelize
data object MoreScreen : TabScreen {
    override val tag: String = "more"
}
