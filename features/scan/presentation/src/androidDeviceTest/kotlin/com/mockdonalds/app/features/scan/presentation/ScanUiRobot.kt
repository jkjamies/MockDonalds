package com.mockdonalds.app.features.scan.presentation

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
import com.mockdonalds.app.features.scan.api.ui.ScanTestTags

class ScanUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = ScanStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: ScanUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { ScanUi(state = state) }
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

    fun setContentWithNoMember() {
        setContentWith(stateRobot.stateWithNoMember())
    }

    fun setContentWithNoProgress() {
        setContentWith(stateRobot.stateWithNoProgress())
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertMemberCardDisplayed()
        assertRewardsProgressDisplayed()
        assertPayNowButtonDisplayed()
        assertViewOffersButtonDisplayed()
        assertProTipDisplayed()
    }

    fun assertLandscapeScreen() {
        assertMemberCardDisplayed()
        assertPayNowButtonDisplayed()
        assertViewOffersButtonDisplayed()
        assertProTipDisplayed()
    }

    fun assertScreenWithNoMember() {
        assertMemberCardNotDisplayed()
        assertPayNowButtonDisplayed()
        assertViewOffersButtonDisplayed()
        assertProTipDisplayed()
    }

    fun assertScreenWithNoProgress() {
        assertMemberCardDisplayed()
        assertRewardsProgressNotDisplayed()
        assertPayNowButtonDisplayed()
        assertViewOffersButtonDisplayed()
    }

    // --- Element Assertions ---

    private fun assertMemberCardDisplayed() {
        rule.onNodeWithTag(ScanTestTags.MEMBER_CARD).assertIsDisplayed()
    }

    private fun assertMemberCardNotDisplayed() {
        rule.onNodeWithTag(ScanTestTags.MEMBER_CARD).assertDoesNotExist()
    }

    private fun assertRewardsProgressDisplayed() {
        rule.onNodeWithTag(ScanTestTags.REWARDS_PROGRESS).performScrollTo().assertIsDisplayed()
    }

    private fun assertRewardsProgressNotDisplayed() {
        rule.onNodeWithTag(ScanTestTags.REWARDS_PROGRESS).assertDoesNotExist()
    }

    private fun assertPayNowButtonDisplayed() {
        rule.onNodeWithTag(ScanTestTags.PAY_NOW_BUTTON).performScrollTo().assertIsDisplayed()
    }

    private fun assertViewOffersButtonDisplayed() {
        rule.onNodeWithTag(ScanTestTags.VIEW_OFFERS_BUTTON).performScrollTo().assertIsDisplayed()
    }

    private fun assertProTipDisplayed() {
        rule.onNodeWithTag(ScanTestTags.PRO_TIP).performScrollTo().assertIsDisplayed()
    }

    // --- Actions ---

    fun tapPayNow() {
        rule.onNodeWithTag(ScanTestTags.PAY_NOW_BUTTON).performScrollTo().performClick()
    }

    fun tapViewOffers() {
        rule.onNodeWithTag(ScanTestTags.VIEW_OFFERS_BUTTON).performScrollTo().performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: ScanEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
