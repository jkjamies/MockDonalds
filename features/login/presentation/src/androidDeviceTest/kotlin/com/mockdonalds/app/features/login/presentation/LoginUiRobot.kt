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
        assertPasswordInputDisplayed()
        assertSignInButtonDisplayed()
        assertSocialButtonsDisplayed()
        assertSignUpLinkDisplayed()
    }

    // --- Element Assertions ---

    private fun assertBrandingDisplayed() {
        rule.onNodeWithTag(LoginTestTags.BRANDING).assertIsDisplayed()
        rule.onNodeWithText("MockDonalds").assertIsDisplayed()
    }

    private fun assertEmailInputDisplayed() {
        rule.onNodeWithTag(LoginTestTags.EMAIL_INPUT).assertIsDisplayed()
    }

    private fun assertPasswordInputDisplayed() {
        rule.onNodeWithTag(LoginTestTags.PASSWORD_INPUT).performScrollTo().assertIsDisplayed()
    }

    private fun assertSignInButtonDisplayed() {
        rule.onNodeWithTag(LoginTestTags.SIGN_IN_BUTTON).performScrollTo().assertIsDisplayed()
    }

    private fun assertSocialButtonsDisplayed() {
        rule.onNodeWithTag(LoginTestTags.APPLE_BUTTON).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(LoginTestTags.GOOGLE_BUTTON).performScrollTo().assertIsDisplayed()
    }

    private fun assertSignUpLinkDisplayed() {
        rule.onNodeWithTag(LoginTestTags.SIGN_UP_LINK).performScrollTo().assertIsDisplayed()
    }

    // --- Actions ---

    fun tapSignInButton() {
        rule.onNodeWithTag(LoginTestTags.SIGN_IN_BUTTON).performScrollTo().performClick()
    }

    fun tapForgotPassword() {
        rule.onNodeWithTag(LoginTestTags.FORGOT_PASSWORD).performScrollTo().performClick()
    }

    fun tapAppleButton() {
        rule.onNodeWithTag(LoginTestTags.APPLE_BUTTON).performScrollTo().performClick()
    }

    fun tapGoogleButton() {
        rule.onNodeWithTag(LoginTestTags.GOOGLE_BUTTON).performScrollTo().performClick()
    }

    fun tapSignUpLink() {
        rule.onNodeWithTag(LoginTestTags.SIGN_UP_LINK).performScrollTo().performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: LoginEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
