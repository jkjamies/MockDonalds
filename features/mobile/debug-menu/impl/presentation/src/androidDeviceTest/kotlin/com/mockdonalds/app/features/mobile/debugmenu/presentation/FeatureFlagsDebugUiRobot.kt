package com.mockdonalds.app.features.mobile.debugmenu.presentation

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
import com.mockdonalds.app.features.mobile.debugmenu.api.ui.FeatureFlagsDebugTestTags

class FeatureFlagsDebugUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = FeatureFlagsDebugStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: FeatureFlagsDebugUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { FeatureFlagsDebugUi(state = state) }
            }
        }
    }

    fun setDefaultContent() {
        setContentWith(stateRobot.defaultState())
    }

    fun setLandscapeContent() {
        setContentWith(stateRobot.defaultState(), landscape = true)
    }

    fun assertDefaultScreen() {
        rule.onNodeWithTag(FeatureFlagsDebugTestTags.ROOT).assertIsDisplayed()
    }

    fun assertLandscapeScreen() {
        rule.onNodeWithTag(FeatureFlagsDebugTestTags.ROOT).assertIsDisplayed()
    }

    fun tapBack() {
        rule.onNodeWithTag(FeatureFlagsDebugTestTags.BACK_BUTTON).performClick()
    }

    fun assertLastEvent(expected: FeatureFlagsDebugEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
