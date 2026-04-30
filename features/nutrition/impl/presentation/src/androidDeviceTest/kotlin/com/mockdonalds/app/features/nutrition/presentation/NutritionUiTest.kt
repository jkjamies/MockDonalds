package com.mockdonalds.app.features.nutrition.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class NutritionUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { NutritionUiRobot(composeTestRule) }

    @Test
    fun rendersScaffoldWithTitleAndWebView() {
        robot.setDefaultContent()
        robot.assertScreenRendered()
        robot.assertWebViewPresent()
    }

    @Test
    fun rendersScaffoldWithoutWebViewWhenLoading() {
        robot.setLoadingContent()
        robot.assertScreenRendered()
    }

    @Test
    fun backButtonEmitsBackClickedEvent() {
        robot.setDefaultContent()
        robot.tapBackButton()
        robot.assertLastEvent(NutritionEvent.BackClicked)
    }
}
