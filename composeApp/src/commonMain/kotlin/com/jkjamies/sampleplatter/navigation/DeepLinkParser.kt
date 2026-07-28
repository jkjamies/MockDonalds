package com.jkjamies.sampleplatter.navigation

import com.jkjamies.sampleplatter.core.circuit.TabScreen
import com.jkjamies.sampleplatter.features.home.api.navigation.HomeScreen
import com.jkjamies.sampleplatter.features.login.api.navigation.LoginScreen
import com.jkjamies.sampleplatter.features.more.api.navigation.MoreScreen
import com.jkjamies.sampleplatter.features.order.api.navigation.OrderScreen
import com.jkjamies.sampleplatter.features.profile.api.navigation.ProfileScreen
import com.jkjamies.sampleplatter.features.rewards.api.navigation.RewardsScreen
import com.jkjamies.sampleplatter.features.scan.api.navigation.ScanScreen
import com.slack.circuit.runtime.screen.Screen

private val tabScreens: List<TabScreen> = listOf(
    HomeScreen, OrderScreen, RewardsScreen, ScanScreen, MoreScreen,
)

fun findTabByTag(tag: String): TabScreen? = tabScreens.firstOrNull { it.tag == tag }

class DeepLinkParser(
    private val screenRegistry: Map<String, () -> Screen>,
) {
    fun parse(uri: String): List<Screen>? {
        val path = uri
            .substringAfter("://", "")
            .substringAfter("/", "")
            .trim('/')

        if (path.isEmpty()) return null

        val screens = path.split("/").mapNotNull { segment ->
            screenRegistry[segment]?.invoke()
        }

        return screens.ifEmpty { null }
    }
}

fun createDeepLinkParser(): DeepLinkParser = DeepLinkParser(
    screenRegistry = tabScreens.associate { it.tag to { it } } + mapOf(
        "profile" to { ProfileScreen },
        "login" to { LoginScreen() },
    ),
)
