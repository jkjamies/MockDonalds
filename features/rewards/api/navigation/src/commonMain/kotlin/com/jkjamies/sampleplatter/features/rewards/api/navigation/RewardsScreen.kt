package com.jkjamies.sampleplatter.features.rewards.api.navigation

import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.jkjamies.sampleplatter.core.circuit.TabScreen

@Parcelize
data object RewardsScreen : TabScreen {
    override val tag: String = "rewards"
}
