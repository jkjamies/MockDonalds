package com.mockdonalds.app.features.nutrition.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.SamplePlatterTheme
import com.mockdonalds.app.features.nutrition.api.ui.NutritionTestTags

class NutritionUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = NutritionStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: NutritionUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                SamplePlatterTheme { NutritionUi(state = state) }
            }
        }
    }

    fun setDefaultContent() = setContentWith(stateRobot.defaultState())
    fun setLoadingContent() = setContentWith(stateRobot.loadingState())
    fun setLandscapeContent() = setContentWith(stateRobot.defaultState(), landscape = true)

    fun assertScreenRendered() {
        rule.onNodeWithTag(NutritionTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithText("Nutrition").assertIsDisplayed()
    }

    fun assertWebViewPresent() {
        rule.onNodeWithTag(NutritionTestTags.WEBVIEW).assertIsDisplayed()
    }

    // NutritionUi has no orientation branch — this guards that the screen survives a landscape
    // window rather than that a distinct compact layout renders. If one is added, this must
    // assert what differs.
    fun assertLandscapeScreen() {
        rule.onNodeWithTag(NutritionTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(NutritionTestTags.WEBVIEW).assertIsDisplayed()
    }

    fun tapBackButton() {
        rule.onNodeWithTag(NutritionTestTags.BACK_BUTTON).performClick()
    }

    fun assertLastEvent(expected: NutritionEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
