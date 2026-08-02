package com.jkjamies.sampleplatter.features.scan.api.navigation

import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.jkjamies.sampleplatter.core.circuit.TabScreen

@Parcelize
data object ScanScreen : TabScreen {
    override val tag: String = "scan"
}
