package com.mockdonalds.app.features.more.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.more.api.ui.MoreTestTags

class MoreUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = MoreStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: MoreUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { MoreUi(state = state) }
            }
        }
    }

    // --- State + Content ---

    fun setDefaultContent() {
        setContentWith(stateRobot.defaultState())
    }

    fun setContentWithNoProfile() {
        setContentWith(stateRobot.stateWithNoProfile())
    }

    fun setContentWithEmptyMenu() {
        setContentWith(stateRobot.stateWithEmptyMenu())
    }

    fun setLandscapeContent() {
        setContentWith(stateRobot.defaultState(), landscape = true)
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertProfileSectionDisplayed()
        assertMenuListDisplayed()
        assertMenuItemDisplayed("1")
        assertMenuItemDisplayed("2")
        assertJoinTeamBannerDisplayed()
    }

    fun assertScreenWithNoProfile() {
        assertProfileSectionNotDisplayed()
        assertMenuListDisplayed()
        assertJoinTeamBannerDisplayed()
    }

    fun assertScreenWithEmptyMenu() {
        assertProfileSectionDisplayed()
        assertMenuListNotDisplayed()
        assertJoinTeamBannerDisplayed()
    }

    fun assertLandscapeScreen() {
        assertProfileSectionDisplayed()
        assertMenuListDisplayed()
        assertJoinTeamBannerDisplayed()
    }

    // --- Element Assertions ---

    private fun assertProfileSectionDisplayed() {
        rule.onNodeWithTag(MoreTestTags.PROFILE_SECTION).assertIsDisplayed()
    }

    private fun assertProfileSectionNotDisplayed() {
        rule.onNodeWithTag(MoreTestTags.PROFILE_SECTION).assertDoesNotExist()
    }

    private fun assertMenuListDisplayed() {
        rule.onNodeWithTag(MoreTestTags.MENU_LIST).assertIsDisplayed()
    }

    private fun assertMenuListNotDisplayed() {
        rule.onNodeWithTag(MoreTestTags.MENU_LIST).assertDoesNotExist()
    }

    private fun assertMenuItemDisplayed(id: String) {
        rule.onNodeWithTag("${MoreTestTags.MENU_ITEM}-$id").assertIsDisplayed()
    }

    private fun assertJoinTeamBannerDisplayed() {
        rule.onNodeWithTag(MoreTestTags.JOIN_TEAM_BANNER).performScrollTo().assertIsDisplayed()
    }

    // --- Actions ---

    fun tapProfile() {
        rule.onNodeWithTag(MoreTestTags.PROFILE_SECTION).performClick()
    }

    fun tapMenuItem(id: String) {
        rule.onNodeWithTag("${MoreTestTags.MENU_ITEM}-$id").performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: MoreEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
