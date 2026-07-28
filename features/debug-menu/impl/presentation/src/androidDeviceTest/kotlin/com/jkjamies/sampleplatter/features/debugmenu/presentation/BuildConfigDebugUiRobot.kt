package com.jkjamies.sampleplatter.features.debugmenu.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.jkjamies.sampleplatter.core.theme.LocalWindowSizeClass
import com.jkjamies.sampleplatter.core.theme.SamplePlatterTheme
import com.jkjamies.sampleplatter.features.debugmenu.api.ui.BuildConfigDebugTestTags

class BuildConfigDebugUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = BuildConfigDebugStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: BuildConfigDebugUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                SamplePlatterTheme { BuildConfigDebugUi(state = state) }
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
        rule.onNodeWithTag(BuildConfigDebugTestTags.ROOT).assertIsDisplayed()
    }

    fun assertLandscapeScreen() {
        rule.onNodeWithTag(BuildConfigDebugTestTags.ROOT).assertIsDisplayed()
    }

    fun tapBack() {
        rule.onNodeWithTag(BuildConfigDebugTestTags.BACK_BUTTON).performClick()
    }

    fun assertLastEvent(expected: BuildConfigDebugEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
