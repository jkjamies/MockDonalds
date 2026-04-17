package com.mockdonalds.app.navint.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.mockdonalds.app.features.more.api.ui.MoreTestTags
import com.mockdonalds.app.features.recents.api.navigation.RecentsScreen
import com.mockdonalds.app.features.recents.api.ui.RecentsTestTags
import com.mockdonalds.app.navint.TestApplication
import com.mockdonalds.app.navint.setNavIntContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests forward and backward navigation for the Recents screen.
 * Uses real presenters with fake data layer bindings.
 */
@RunWith(AndroidJUnit4::class)
class RecentsNavigationTest {

    @get:Rule
    val rule = createComposeRule()

    private val graph
        get() = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApplication).graph

    @Test
    fun recentsScreenRendersWithRealPresenter() {
        rule.setNavIntContent(
            circuit = graph.circuit,
            root = RecentsScreen,
        )

        rule.onNodeWithTag(RecentsTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(RecentsTestTags.LIST).assertIsDisplayed()
        rule.onNodeWithTag(RecentsTestTags.BACK_BUTTON).assertIsDisplayed()
    }

    @Test
    fun navigateFromMoreToRecents() {
        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = MoreScreen,
            onNavigator = { testNavigator = it },
        )

        rule.onNodeWithTag(MoreTestTags.MENU_LIST).assertIsDisplayed()

        rule.runOnUiThread {
            testNavigator!!.goTo(RecentsScreen)
        }

        rule.waitForIdle()
        rule.onNodeWithTag(RecentsTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(RecentsTestTags.LIST).assertIsDisplayed()
    }

    @Test
    fun navigateBackFromRecentsToMore() {
        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = MoreScreen,
            onNavigator = { testNavigator = it },
        )

        rule.runOnUiThread {
            testNavigator!!.goTo(RecentsScreen)
        }

        rule.waitForIdle()
        rule.onNodeWithTag(RecentsTestTags.SCREEN).assertIsDisplayed()

        rule.runOnUiThread {
            testNavigator!!.pop()
        }

        rule.waitForIdle()
        rule.onNodeWithTag(MoreTestTags.MENU_LIST).assertIsDisplayed()
    }
}
