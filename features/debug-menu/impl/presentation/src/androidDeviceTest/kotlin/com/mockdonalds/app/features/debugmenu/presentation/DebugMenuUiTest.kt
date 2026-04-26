package com.mockdonalds.app.features.debugmenu.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class DebugMenuUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { DebugMenuUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersWithNoEntries() {
        robot.setContentWithNoEntries()
        robot.assertScreenWithNoEntries()
    }

    @Test
    fun rendersLandscapeLayout() {
        robot.setLandscapeContent()
        robot.assertLandscapeScreen()
    }

    @Test
    fun entryTapEmitsEvent() {
        robot.setDefaultContent()
        robot.tapEntry("feature-flags")
        robot.assertLastEvent(DebugMenuEvent.EntryClicked("feature-flags"))
    }

    @Test
    fun backTapEmitsEvent() {
        robot.setDefaultContent()
        robot.tapBack()
        robot.assertLastEvent(DebugMenuEvent.BackClicked)
    }
}
