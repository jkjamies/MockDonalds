package com.jkjamies.sampleplatter.features.more.api.navigation

import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.jkjamies.sampleplatter.core.circuit.TabScreen

@Parcelize
data object MoreScreen : TabScreen {
    override val tag: String = "more"
}
