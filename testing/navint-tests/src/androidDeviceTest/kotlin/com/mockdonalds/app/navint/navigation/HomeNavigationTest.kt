package com.mockdonalds.app.navint.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.mockdonalds.app.features.home.api.ui.HomeTestTags
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.mockdonalds.app.features.order.api.ui.OrderTestTags
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.mockdonalds.app.features.rewards.api.ui.RewardsTestTags
import com.mockdonalds.app.navint.TestApplication
import com.mockdonalds.app.navint.setNavIntContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that screens render correctly when navigated to via Circuit.
 * Uses real presenters with fake data layer bindings.
 */
@RunWith(AndroidJUnit4::class)
class HomeNavigationTest {

    @get:Rule
    val rule = createComposeRule()

    private val graph
        get() = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApplication).graph

    @Test
    fun homeScreenRendersWithRealPresenter() {
        rule.setNavIntContent(
            circuit = graph.circuit,
            root = HomeScreen,
        )

        rule.onNodeWithTag(HomeTestTags.USER_NAME).assertIsDisplayed()
        rule.onNodeWithTag(HomeTestTags.HERO_BANNER).assertIsDisplayed()
    }

    @Test
    fun orderScreenRendersWithRealPresenter() {
        rule.setNavIntContent(
            circuit = graph.circuit,
            root = OrderScreen,
        )

        rule.onNodeWithTag(OrderTestTags.FEATURED_ITEMS_SECTION).assertIsDisplayed()
    }

    @Test
    fun rewardsScreenRendersWithRealPresenter() {
        rule.setNavIntContent(
            circuit = graph.circuit,
            root = RewardsScreen,
        )

        rule.onNodeWithTag(RewardsTestTags.POINTS_SECTION).assertIsDisplayed()
    }

    @Test
    fun navigateFromHomeToOrder() {
        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = HomeScreen,
            onNavigator = { testNavigator = it },
        )

        rule.onNodeWithTag(HomeTestTags.USER_NAME).assertIsDisplayed()

        rule.runOnUiThread {
            testNavigator!!.resetRoot(OrderScreen)
        }

        rule.waitForIdle()
        rule.onNodeWithTag(OrderTestTags.FEATURED_ITEMS_SECTION).assertIsDisplayed()
    }
}
