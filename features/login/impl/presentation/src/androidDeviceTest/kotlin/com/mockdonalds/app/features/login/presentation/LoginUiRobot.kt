package com.mockdonalds.app.features.login.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.login.api.ui.LoginTestTags

class LoginUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = LoginStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: LoginUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { LoginUi(state = state) }
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

    fun setContentWithEmail(email: String) {
        setContentWith(stateRobot.stateWithEmail(email))
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertBrandingDisplayed()
        assertEmailInputDisplayed()
        assertSignInButtonDisplayed()
        assertSocialButtonsDisplayed()
    }

    fun assertLandscapeScreen() {
        assertBrandingDisplayed()
        assertEmailInputDisplayed()
        assertSignInButtonDisplayed()
        assertSocialButtonsDisplayed()
    }

    // --- Element Assertions ---

    private fun assertBrandingDisplayed() {
        rule.onNodeWithTag(LoginTestTags.BRANDING).assertIsDisplayed()
        rule.onNodeWithText("MockDonalds").assertIsDisplayed()
    }

    private fun assertEmailInputDisplayed() {
        rule.onNodeWithTag(LoginTestTags.EMAIL_INPUT).assertIsDisplayed()
    }

    private fun assertSignInButtonDisplayed() {
        rule.onNodeWithTag(LoginTestTags.SIGN_IN_BUTTON).performScrollTo().assertIsDisplayed()
    }

    private fun assertSocialButtonsDisplayed() {
        rule.onNodeWithTag(LoginTestTags.GOOGLE_BUTTON).performScrollTo().assertIsDisplayed()
    }

    // --- Actions ---

    fun tapSignInButton() {
        rule.onNodeWithTag(LoginTestTags.SIGN_IN_BUTTON).performScrollTo().performClick()
    }

    fun tapGoogleButton() {
        rule.onNodeWithTag(LoginTestTags.GOOGLE_BUTTON).performScrollTo().performClick()
    }

    fun tapDialogConfirm() {
        rule.onNodeWithText("Send Link").performClick()
    }

    // --- Dialog Assertions ---

    fun assertSignInDialogDisplayed() {
        rule.onNodeWithTag(LoginTestTags.SIGN_IN_DIALOG).assertIsDisplayed()
        rule.onNodeWithText("Send Link").assertIsDisplayed()
        rule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: LoginEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
