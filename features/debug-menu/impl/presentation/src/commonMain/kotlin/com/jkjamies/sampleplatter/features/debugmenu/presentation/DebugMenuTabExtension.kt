package com.jkjamies.sampleplatter.features.debugmenu.presentation

import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.DebugMenuScreen
import com.jkjamies.sampleplatter.features.more.api.navigation.MoreTabExtension
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class DebugMenuTabExtension : MoreTabExtension {
    override val id: String = "debug-menu"
    override val icon: String = "🐞"
    override val title: String = "Debug Menu"
    override val isDebugOnly: Boolean = true

    override fun onClick(navigator: Navigator) {
        navigator.goTo(DebugMenuScreen)
    }
}
