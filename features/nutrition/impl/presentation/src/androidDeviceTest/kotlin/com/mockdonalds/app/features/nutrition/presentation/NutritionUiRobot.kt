package com.mockdonalds.app.features.nutrition.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.nutrition.api.ui.NutritionTestTags

class NutritionUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = NutritionStateRobot()

    private fun setContentWith(state: NutritionUiState) {
        rule.setContent {
            MockDonaldsTheme { NutritionUi(state = state) }
        }
    }

    fun setDefaultContent() = setContentWith(stateRobot.defaultState())
    fun setLoadingContent() = setContentWith(stateRobot.loadingState())

    fun assertScreenRendered() {
        rule.onNodeWithTag(NutritionTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithText("Nutrition").assertIsDisplayed()
    }

    fun assertWebViewPresent() {
        rule.onNodeWithTag(NutritionTestTags.WEBVIEW).assertIsDisplayed()
    }

    fun tapBackButton() {
        rule.onNodeWithTag(NutritionTestTags.BACK_BUTTON).performClick()
    }

    fun assertLastEvent(expected: NutritionEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
