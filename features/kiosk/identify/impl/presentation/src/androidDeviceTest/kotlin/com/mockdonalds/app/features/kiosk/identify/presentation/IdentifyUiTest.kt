package com.mockdonalds.app.features.kiosk.identify.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class IdentifyUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { IdentifyUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultScreen() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersSubmittingOverlay() {
        robot.setSubmittingContent()
        robot.assertSubmittingOverlay()
    }

    @Test
    fun rendersErrorBanner() {
        robot.setErrorContent()
        robot.assertErrorBanner()
    }

    @Test
    fun hidesSkipWhenDisabled() {
        robot.setSkipDisabledContent()
        robot.assertSkipHidden()
    }

    @Test
    fun rendersLandscapeLayout() {
        robot.setLandscapeContent()
        robot.assertLandscapeScreen()
    }

    @Test
    fun keypadDispatchesDigitEvents() {
        robot.setDefaultContent()
        robot.tapDigit('5')
        robot.assertCapturedEvents(IdentifyEvent.DigitPressed('5'))
    }
}
