package com.mockdonalds.app.features.more.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class MoreUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { MoreUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersWithNoProfile() {
        robot.setContentWithNoProfile()
        robot.assertScreenWithNoProfile()
    }

    @Test
    fun rendersWithEmptyMenu() {
        robot.setContentWithEmptyMenu()
        robot.assertScreenWithEmptyMenu()
    }

    @Test
    fun profileTapEmitsEvent() {
        robot.setDefaultContent()
        robot.tapProfile()
        robot.assertLastEvent(MoreEvent.ProfileClicked)
    }

    @Test
    fun menuItemTapEmitsEvent() {
        robot.setDefaultContent()
        robot.tapMenuItem("1")
        robot.assertLastEvent(MoreEvent.MenuItemClicked("1"))
    }
}
