package com.mockdonalds.app.features.kiosk.attract.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class AttractUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { AttractUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersEmptyAdsFallback() {
        robot.setEmptyAdsContent()
        robot.assertEmptyAdsFallback()
    }

    @Test
    fun rendersLandscapeLayout() {
        robot.setLandscapeContent()
        robot.assertLandscapeScreen()
    }

    @Test
    fun tapAnywhereEmitsTouchToOrder() {
        robot.setDefaultContent()
        robot.tapAnywhere()
        robot.assertLastEvent(AttractEvent.TouchToOrder)
    }
}
