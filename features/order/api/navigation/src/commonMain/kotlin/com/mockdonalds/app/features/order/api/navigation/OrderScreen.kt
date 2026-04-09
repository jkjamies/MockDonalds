package com.mockdonalds.app.features.order.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.core.circuit.TabScreen

@Parcelize
data object OrderScreen : TabScreen {
    override val tag: String = "order"
}
