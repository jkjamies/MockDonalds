package com.mockdonalds.app.features.mobile.debugmenu.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class FeatureFlagsDebugUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { FeatureFlagsDebugUiRobot(composeTestRule) }

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
    fun backTapEmitsEvent() {
        robot.setDefaultContent()
        robot.tapBack()
        robot.assertLastEvent(FeatureFlagsDebugEvent.BackClicked)
    }
}
