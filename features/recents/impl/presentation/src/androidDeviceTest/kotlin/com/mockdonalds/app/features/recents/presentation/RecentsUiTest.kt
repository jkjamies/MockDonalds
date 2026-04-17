package com.mockdonalds.app.features.recents.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class RecentsUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { RecentsUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersEmptyState() {
        robot.setEmptyContent()
        robot.assertEmptyScreen()
    }

    @Test
    fun rendersLandscapeLayout() {
        robot.setLandscapeContent()
        robot.assertLandscapeScreen()
    }

    @Test
    fun backButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapBackButton()
        robot.assertLastEvent(RecentsEvent.OnBackTapped)
    }

    @Test
    fun itemTapEmitsEvent() {
        robot.setDefaultContent()
        robot.tapItem("1")
        robot.assertLastEvent(RecentsEvent.OnItemTapped("1"))
    }
}
