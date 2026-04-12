package com.mockdonalds.app.features.login.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class WelcomeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { WelcomeUiRobot(composeTestRule) }

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
    fun continueButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapContinueButton()
        robot.assertLastEvent(WelcomeEvent.ContinueClicked)
    }
}
