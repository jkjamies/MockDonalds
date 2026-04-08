package com.mockdonalds.app.features.login.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.login.api.ui.LoginTestTags

class LoginUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = LoginStateRobot()

    // --- State + Content ---

    fun setDefaultContent() {
        val state = stateRobot.defaultState()
        rule.setContent { MockDonaldsTheme { LoginUi(state = state) } }
    }

    fun setContentWithEmail(email: String) {
        val state = stateRobot.stateWithEmail(email)
        rule.setContent { MockDonaldsTheme { LoginUi(state = state) } }
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
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

    // --- Event Verification ---

    fun assertLastEvent(expected: LoginEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
