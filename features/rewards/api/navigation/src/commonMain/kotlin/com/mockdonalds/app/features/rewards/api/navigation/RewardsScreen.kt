package com.mockdonalds.app.features.rewards.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.mockdonalds.app.core.circuit.TabScreen

@Parcelize
data object RewardsScreen : TabScreen {
    override val tag: String = "rewards"
}
