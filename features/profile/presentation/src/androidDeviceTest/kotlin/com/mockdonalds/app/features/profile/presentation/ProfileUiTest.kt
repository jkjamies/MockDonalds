package com.mockdonalds.app.features.profile.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ProfileUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { ProfileUiRobot(composeTestRule) }

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
    fun logoutButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapLogoutButton()
        robot.assertLastEvent(ProfileEvent.LogoutClicked)
    }
}
