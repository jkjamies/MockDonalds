package com.mockdonalds.app.features.scan.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.scan.api.ui.ScanTestTags

class ScanUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = ScanStateRobot()

    // --- State + Content ---

    fun setDefaultContent() {
        val state = stateRobot.defaultState()
        rule.setContent { MockDonaldsTheme { ScanUi(state = state) } }
    }

    fun setContentWithNoMember() {
        val state = stateRobot.stateWithNoMember()
        rule.setContent { MockDonaldsTheme { ScanUi(state = state) } }
    }

    fun setContentWithNoProgress() {
        val state = stateRobot.stateWithNoProgress()
        rule.setContent { MockDonaldsTheme { ScanUi(state = state) } }
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertMemberCardDisplayed()
        assertRewardsProgressDisplayed()
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
