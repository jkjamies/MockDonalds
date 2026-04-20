package com.mockdonalds.app.features.more.api.navigation

import com.slack.circuit.runtime.Navigator

interface MoreTabExtension {
    val id: String
    val icon: String
    val title: String
    val isDebugOnly: Boolean get() = false
    fun onClick(navigator: Navigator)
}
