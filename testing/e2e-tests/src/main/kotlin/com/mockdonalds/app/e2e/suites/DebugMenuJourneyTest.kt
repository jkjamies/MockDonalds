package com.mockdonalds.app.e2e.suites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mockdonalds.app.e2e.robots.AppRobot
import com.mockdonalds.app.features.debugmenu.api.ui.BuildConfigDebugTestTags
import com.mockdonalds.app.features.debugmenu.api.ui.DebugMenuTestTags
import com.mockdonalds.app.features.debugmenu.api.ui.FeatureFlagsDebugTestTags
import com.mockdonalds.app.features.more.api.ui.MoreTestTags
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Debug-menu journey — exercises the engineer-only hub that is contributed into the
 * More tab via `MoreTabExtension` with `isDebugOnly = true`. The extension is only
 * visible when `AppBuildConfig.isDebug` is true, which is the case for all e2e runs
 * (they target the Debug build). This journey verifies the hub and both sub-screens
 * (feature flags + build config) render and surface their TestTags.
 */
@RunWith(AndroidJUnit4::class)
class DebugMenuJourneyTest {

    private val robot = AppRobot()

    @Before
    fun setUp() {
        robot.launchApp()
    }

    @Test
    fun opensDebugMenuFromMoreTab() {
        robot.tapTab("More")
        robot.assertElementDisplayed(MoreTestTags.MENU_LIST)
        robot.tapElement("${MoreTestTags.MENU_ITEM}-debug-menu")

        robot.assertElementDisplayed(DebugMenuTestTags.ROOT)
        robot.assertElementDisplayed(DebugMenuTestTags.ENTRY_LIST)
        robot.assertElementDisplayed("${DebugMenuTestTags.ENTRY_ITEM}-feature-flags")
        robot.assertElementDisplayed("${DebugMenuTestTags.ENTRY_ITEM}-build-config")
    }

    @Test
    fun opensFeatureFlagsDebugScreen() {
        robot.tapTab("More")
        robot.tapElement("${MoreTestTags.MENU_ITEM}-debug-menu")
        robot.tapElement("${DebugMenuTestTags.ENTRY_ITEM}-feature-flags")

        robot.assertElementDisplayed(FeatureFlagsDebugTestTags.ROOT)
    }

    @Test
    fun opensBuildConfigDebugScreen() {
        robot.tapTab("More")
        robot.tapElement("${MoreTestTags.MENU_ITEM}-debug-menu")
        robot.tapElement("${DebugMenuTestTags.ENTRY_ITEM}-build-config")

        robot.assertElementDisplayed(BuildConfigDebugTestTags.ROOT)
    }
}
