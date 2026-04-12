package com.mockdonalds.app.features.login.presentation

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
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.login.api.ui.WelcomeTestTags

class WelcomeUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = WelcomeStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: WelcomeUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { WelcomeUi(state = state) }
            }
        }
    }

    // --- State + Content ---

    fun setDefaultContent() {
        setContentWith(stateRobot.defaultState())
    }

    fun setLandscapeContent() {
        setContentWith(stateRobot.defaultState(), landscape = true)
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertLogoDisplayed()
        assertTitleDisplayed()
        assertSubtitleDisplayed()
        assertContinueButtonDisplayed()
    }

    fun assertLandscapeScreen() {
        assertLogoDisplayed()
        assertTitleDisplayed()
        assertSubtitleDisplayed()
        assertContinueButtonDisplayed()
    }

    // --- Element Assertions ---

    private fun assertLogoDisplayed() {
        rule.onNodeWithTag(WelcomeTestTags.LOGO).assertIsDisplayed()
    }

    private fun assertTitleDisplayed() {
        rule.onNodeWithTag(WelcomeTestTags.TITLE).assertIsDisplayed()
        rule.onNodeWithText("Welcome!").assertIsDisplayed()
    }

    private fun assertSubtitleDisplayed() {
        rule.onNodeWithTag(WelcomeTestTags.SUBTITLE).assertIsDisplayed()
        rule.onNodeWithText("You're all set").assertIsDisplayed()
    }

    private fun assertContinueButtonDisplayed() {
        rule.onNodeWithTag(WelcomeTestTags.CONTINUE_BUTTON).assertIsDisplayed()
        rule.onNodeWithText("Continue").assertIsDisplayed()
    }

    // --- Actions ---

    fun tapContinueButton() {
        rule.onNodeWithTag(WelcomeTestTags.CONTINUE_BUTTON).performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: WelcomeEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
