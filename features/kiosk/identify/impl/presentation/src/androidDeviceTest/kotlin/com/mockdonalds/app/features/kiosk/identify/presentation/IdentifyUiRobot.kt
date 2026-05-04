package com.mockdonalds.app.features.kiosk.identify.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.kiosk.identify.api.ui.IdentifyTestTags

class IdentifyUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = IdentifyStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: IdentifyUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(960.dp, 540.dp) else DpSize(540.dp, 960.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { IdentifyUi(state = state) }
            }
        }
    }

    fun setDefaultContent() = setContentWith(stateRobot.defaultState())
    fun setLandscapeContent() = setContentWith(stateRobot.defaultState(), landscape = true)
    fun setSubmittingContent() = setContentWith(stateRobot.submittingState())
    fun setErrorContent() = setContentWith(stateRobot.errorState())
    fun setSkipDisabledContent() = setContentWith(stateRobot.skipDisabledState())

    fun assertDefaultScreen() {
        rule.onNodeWithTag(IdentifyTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(IdentifyTestTags.QR_SCANNER).assertIsDisplayed()
        rule.onNodeWithTag(IdentifyTestTags.PHONE_DISPLAY).assertIsDisplayed()
        rule.onNodeWithTag(IdentifyTestTags.SKIP).assertIsDisplayed()
        rule.onNodeWithTag(IdentifyTestTags.SUBMIT).assertIsDisplayed()
    }

    fun assertSubmittingOverlay() {
        rule.onNodeWithTag(IdentifyTestTags.LOADING_OVERLAY).assertIsDisplayed()
    }

    fun assertErrorBanner() {
        rule.onNodeWithTag(IdentifyTestTags.ERROR_BANNER).assertIsDisplayed()
    }

    fun assertSkipHidden() {
        rule.onNodeWithTag(IdentifyTestTags.SKIP).assertIsNotDisplayed()
    }

    fun assertLandscapeScreen() {
        rule.onNodeWithTag(IdentifyTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(IdentifyTestTags.PHONE_DISPLAY).assertIsDisplayed()
    }

    fun tapDigit(digit: Char) {
        rule.onNodeWithTag("${IdentifyTestTags.KEYPAD_DIGIT_PREFIX}$digit").performClick()
    }

    fun tapDelete() {
        rule.onNodeWithTag(IdentifyTestTags.DELETE).performClick()
    }

    fun tapSubmit() {
        rule.onNodeWithTag(IdentifyTestTags.SUBMIT).performClick()
    }

    fun tapSkip() {
        rule.onNodeWithTag(IdentifyTestTags.SKIP).performClick()
    }

    fun assertCapturedEvents(vararg expected: IdentifyEvent) {
        val captured = stateRobot.capturedEvents
        org.junit.Assert.assertEquals(expected.toList(), captured.take(expected.size))
    }
}
