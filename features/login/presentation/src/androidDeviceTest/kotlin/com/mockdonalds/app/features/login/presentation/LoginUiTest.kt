package com.mockdonalds.app.features.login.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class LoginUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { LoginUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun signInButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapSignInButton()
        robot.assertLastEvent(LoginEvent.SignInClicked)
    }

    @Test
    fun googleSignInEmitsEvent() {
        robot.setDefaultContent()
        robot.tapGoogleButton()
        robot.assertLastEvent(LoginEvent.GoogleSignInClicked)
    }

}
