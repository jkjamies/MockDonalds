package com.jkjamies.sampleplatter.navint.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.DebugMenuScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.FeatureFlagsDebugScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.ui.BuildConfigDebugTestTags
import com.jkjamies.sampleplatter.features.debugmenu.api.ui.DebugMenuTestTags
import com.jkjamies.sampleplatter.features.debugmenu.api.ui.FeatureFlagsDebugTestTags
import com.jkjamies.sampleplatter.navint.TestApplication
import com.jkjamies.sampleplatter.navint.setNavIntContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DebugMenuNavigationTest {

    @get:Rule
    val rule = createComposeRule()

    private val graph
        get() = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApplication).graph

    @Test
    fun debugMenuScreenRendersWithRealPresenter() {
        rule.setNavIntContent(
            circuit = graph.circuit,
            root = DebugMenuScreen,
        )

        rule.onNodeWithTag(DebugMenuTestTags.ROOT).assertIsDisplayed()
        rule.onNodeWithTag(DebugMenuTestTags.ENTRY_LIST).assertIsDisplayed()
    }

    @Test
    fun navigateFromDebugMenuToFeatureFlags() {
        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = DebugMenuScreen,
            onNavigator = { testNavigator = it },
        )

        rule.onNodeWithTag(DebugMenuTestTags.ENTRY_LIST).assertIsDisplayed()

        rule.runOnUiThread {
            testNavigator!!.goTo(FeatureFlagsDebugScreen)
        }

        rule.waitForIdle()
        rule.onNodeWithTag(FeatureFlagsDebugTestTags.ROOT).assertIsDisplayed()
    }

    @Test
    fun navigateFromDebugMenuToBuildConfig() {
        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = DebugMenuScreen,
            onNavigator = { testNavigator = it },
        )

        rule.runOnUiThread {
            testNavigator!!.goTo(BuildConfigDebugScreen)
        }

        rule.waitForIdle()
        rule.onNodeWithTag(BuildConfigDebugTestTags.ROOT).assertIsDisplayed()
    }

    @Test
    fun navigateBackFromFeatureFlagsToDebugMenu() {
        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = DebugMenuScreen,
            onNavigator = { testNavigator = it },
        )

        rule.runOnUiThread {
            testNavigator!!.goTo(FeatureFlagsDebugScreen)
        }
        rule.waitForIdle()
        rule.onNodeWithTag(FeatureFlagsDebugTestTags.ROOT).assertIsDisplayed()

        rule.runOnUiThread {
            testNavigator!!.pop()
        }
        rule.waitForIdle()

        rule.onNodeWithTag(DebugMenuTestTags.ENTRY_LIST).assertIsDisplayed()
    }
}
