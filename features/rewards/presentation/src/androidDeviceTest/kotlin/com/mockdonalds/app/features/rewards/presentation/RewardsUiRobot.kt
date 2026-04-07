package com.mockdonalds.app.features.rewards.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.rewards.api.ui.RewardsTestTags

class RewardsUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = RewardsStateRobot()

    // --- State + Content ---

    fun setDefaultContent() {
        val state = stateRobot.defaultState()
        rule.setContent { MockDonaldsTheme { RewardsUi(state = state) } }
    }

    fun setContentWithNoProgress() {
        val state = stateRobot.stateWithNoProgress()
        rule.setContent { MockDonaldsTheme { RewardsUi(state = state) } }
    }

    fun setContentWithEmptyVault() {
        val state = stateRobot.stateWithEmptyVault()
        rule.setContent { MockDonaldsTheme { RewardsUi(state = state) } }
    }

    fun setContentWithEmptyHistory() {
        val state = stateRobot.stateWithEmptyHistory()
        rule.setContent { MockDonaldsTheme { RewardsUi(state = state) } }
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertPointsSectionDisplayed()
        assertVaultSpecialsSectionDisplayed()
        assertFeaturedVaultCardDisplayed("1")
        assertHistorySectionDisplayed()
    }

    fun assertScreenWithNoProgress() {
        assertPointsSectionNotDisplayed()
        assertVaultSpecialsSectionDisplayed()
        assertHistorySectionDisplayed()
    }

    fun assertScreenWithEmptyVault() {
        assertPointsSectionDisplayed()
        assertVaultSpecialsSectionNotDisplayed()
        assertHistorySectionDisplayed()
    }

    fun assertScreenWithEmptyHistory() {
        assertPointsSectionDisplayed()
        assertVaultSpecialsSectionDisplayed()
        assertHistorySectionNotDisplayed()
    }

    // --- Element Assertions ---

    private fun assertPointsSectionDisplayed() {
        rule.onNodeWithTag(RewardsTestTags.POINTS_SECTION).assertIsDisplayed()
    }

    private fun assertPointsSectionNotDisplayed() {
        rule.onNodeWithTag(RewardsTestTags.POINTS_SECTION).assertDoesNotExist()
    }

    private fun assertVaultSpecialsSectionDisplayed() {
        rule.onNodeWithTag(RewardsTestTags.VAULT_SPECIALS_SECTION).performScrollTo().assertIsDisplayed()
    }

    private fun assertVaultSpecialsSectionNotDisplayed() {
        rule.onNodeWithTag(RewardsTestTags.VAULT_SPECIALS_SECTION).assertDoesNotExist()
    }

    private fun assertFeaturedVaultCardDisplayed(id: String) {
        rule.onNodeWithTag("${RewardsTestTags.FEATURED_VAULT_CARD}-$id").performScrollTo().assertIsDisplayed()
    }

    private fun assertHistorySectionDisplayed() {
        rule.onNodeWithTag(RewardsTestTags.HISTORY_SECTION).performScrollTo().assertIsDisplayed()
    }

    private fun assertHistorySectionNotDisplayed() {
        rule.onNodeWithTag(RewardsTestTags.HISTORY_SECTION).assertDoesNotExist()
    }

    // --- Actions ---

    fun tapViewAll() {
        rule.onNodeWithTag(RewardsTestTags.VIEW_ALL).performScrollTo().performClick()
    }

    fun tapFeaturedVaultCard(id: String) {
        rule.onNodeWithTag("${RewardsTestTags.FEATURED_VAULT_CARD}-$id").performScrollTo().performClick()
    }

    fun tapVaultSpecialCard(id: String) {
        rule.onNodeWithTag("${RewardsTestTags.VAULT_SPECIAL_CARD}-$id").performScrollTo().performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: RewardsEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
