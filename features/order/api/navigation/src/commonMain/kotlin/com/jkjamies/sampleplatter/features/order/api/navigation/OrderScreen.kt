package com.jkjamies.sampleplatter.features.order.api.navigation

import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.jkjamies.sampleplatter.core.circuit.TabScreen

@Parcelize
data object OrderScreen : TabScreen {
    override val tag: String = "order"
}
