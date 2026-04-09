package com.mockdonalds.app.features.rewards.presentation

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
import com.mockdonalds.app.features.rewards.api.ui.RewardsTestTags

class RewardsUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = RewardsStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: RewardsUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { RewardsUi(state = state) }
            }
        }
    }

    // --- State + Content ---

    fun setDefaultContent() {
        setContentWith(stateRobot.defaultState())
    }

    fun setLandscapeContent() {
        setContentWith(stateRobot.defaultState(), landscape = true)
    }

    fun setContentWithNoProgress() {
        setContentWith(stateRobot.stateWithNoProgress())
    }

    fun setContentWithEmptyVault() {
        setContentWith(stateRobot.stateWithEmptyVault())
    }

    fun setContentWithEmptyHistory() {
        setContentWith(stateRobot.stateWithEmptyHistory())
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertPointsSectionDisplayed()
        assertVaultSpecialsSectionDisplayed()
        assertFeaturedVaultCardDisplayed("1")
        assertHistorySectionDisplayed()
    }

    fun assertLandscapeScreen() {
        assertPointsSectionDisplayed()
        assertVaultSpecialsSectionDisplayed()
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
