package com.mockdonalds.app.features.scan.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ScanUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { ScanUiRobot(composeTestRule) }

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
    fun rendersWithNoMember() {
        robot.setContentWithNoMember()
        robot.assertScreenWithNoMember()
    }

    @Test
    fun rendersWithNoProgress() {
        robot.setContentWithNoProgress()
        robot.assertScreenWithNoProgress()
    }

    @Test
    fun payNowEmitsEvent() {
        robot.setDefaultContent()
        robot.tapPayNow()
        robot.assertLastEvent(ScanEvent.PayNowClicked)
    }

    @Test
    fun viewOffersEmitsEvent() {
        robot.setDefaultContent()
        robot.tapViewOffers()
        robot.assertLastEvent(ScanEvent.ViewOffersClicked)
    }
}
