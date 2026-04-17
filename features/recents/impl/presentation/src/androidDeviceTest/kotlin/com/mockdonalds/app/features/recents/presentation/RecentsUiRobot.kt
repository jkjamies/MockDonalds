package com.mockdonalds.app.features.recents.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.recents.api.ui.RecentsTestTags

class RecentsUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = RecentsStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: RecentsUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { RecentsUi(state = state) }
            }
        }
    }

    fun setDefaultContent() = setContentWith(stateRobot.defaultState())
    fun setLandscapeContent() = setContentWith(stateRobot.defaultState(), landscape = true)
    fun setEmptyContent() = setContentWith(stateRobot.emptyState())

    fun assertDefaultScreen() {
        rule.onNodeWithTag(RecentsTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(RecentsTestTags.LIST).assertIsDisplayed()
        rule.onNodeWithTag("${RecentsTestTags.ITEM}-1").assertIsDisplayed()
        rule.onNodeWithText("Big Mac Combo").assertIsDisplayed()
    }
    
    fun assertEmptyScreen() {
        rule.onNodeWithTag(RecentsTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(RecentsTestTags.EMPTY).assertIsDisplayed()
        rule.onNodeWithText("No recent activity").assertIsDisplayed()
    }

    fun assertLandscapeScreen() {
        rule.onNodeWithTag(RecentsTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(RecentsTestTags.LIST).assertIsDisplayed()
    }

    // --- Actions ---

    fun tapBackButton() {
        rule.onNodeWithTag(RecentsTestTags.BACK_BUTTON).performClick()
    }

    fun tapItem(id: String) {
        rule.onNodeWithTag("${RecentsTestTags.ITEM}-$id").performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: RecentsEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}