package com.mockdonalds.app.features.more.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.more.api.ui.MoreTestTags

class MoreUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = MoreStateRobot()

    // --- State + Content ---

    fun setDefaultContent() {
        val state = stateRobot.defaultState()
        rule.setContent { MockDonaldsTheme { MoreUi(state = state) } }
    }

    fun setContentWithNoProfile() {
        val state = stateRobot.stateWithNoProfile()
        rule.setContent { MockDonaldsTheme { MoreUi(state = state) } }
    }

    fun setContentWithEmptyMenu() {
        val state = stateRobot.stateWithEmptyMenu()
        rule.setContent { MockDonaldsTheme { MoreUi(state = state) } }
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
