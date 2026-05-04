package com.mockdonalds.app.features.mobile.scan.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.core.circuit.TabScreen

@Parcelize
data object ScanScreen : TabScreen {
    override val tag: String = "scan"
}
