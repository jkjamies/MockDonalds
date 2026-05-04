package com.mockdonalds.app.features.kiosk.attract.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.kiosk.attract.api.ui.AttractTestTags

class AttractUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = AttractStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: AttractUiState, landscape: Boolean = false) {
        // Kiosk hardware is portrait-locked; landscape here is for the rule's
        // mandatory rendersLandscapeLayout coverage, not a real deployment mode.
        val size = if (landscape) DpSize(960.dp, 540.dp) else DpSize(540.dp, 960.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { AttractUi(state = state) }
            }
        }
    }

    fun setDefaultContent() = setContentWith(stateRobot.defaultState())
    fun setLandscapeContent() = setContentWith(stateRobot.defaultState(), landscape = true)
    fun setEmptyAdsContent() = setContentWith(stateRobot.emptyAdsState())

    fun assertDefaultScreen() {
        rule.onNodeWithTag(AttractTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(AttractTestTags.PAGER).assertIsDisplayed()
        rule.onNodeWithTag(AttractTestTags.TOUCH_TO_ORDER_OVERLAY).assertIsDisplayed()
    }

    fun assertEmptyAdsFallback() {
        rule.onNodeWithTag(AttractTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(AttractTestTags.TOUCH_TO_ORDER_OVERLAY).assertIsDisplayed()
    }

    fun assertLandscapeScreen() {
        rule.onNodeWithTag(AttractTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(AttractTestTags.TOUCH_TO_ORDER_OVERLAY).assertIsDisplayed()
    }

    fun tapAnywhere() {
        rule.onNodeWithTag(AttractTestTags.SCREEN).performClick()
    }

    fun assertLastEvent(expected: AttractEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
