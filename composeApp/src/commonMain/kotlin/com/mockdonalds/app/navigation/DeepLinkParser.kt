package com.mockdonalds.app.navigation

import com.mockdonalds.app.core.circuit.TabScreen
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.mockdonalds.app.features.login.api.navigation.LoginScreen
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.mockdonalds.app.features.profile.api.navigation.ProfileScreen
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.mockdonalds.app.features.scan.api.navigation.ScanScreen
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
