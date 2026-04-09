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
    fun rendersLandscapeLayout() {
        robot.setLandscapeContent()
        robot.assertLandscapeScreen()
    }

    @Test
    fun signInButtonShowsDialog() {
        robot.setDefaultContent()
        robot.tapSignInButton()
        robot.assertSignInDialogDisplayed()
    }

    @Test
    fun signInDialogConfirmEmitsEvent() {
        robot.setDefaultContent()
        robot.tapSignInButton()
        robot.tapDialogConfirm()
        robot.assertLastEvent(LoginEvent.SignInConfirmed)
    }

    @Test
    fun googleSignInEmitsEvent() {
        robot.setDefaultContent()
        robot.tapGoogleButton()
        robot.assertLastEvent(LoginEvent.GoogleSignInClicked)
    }

}
