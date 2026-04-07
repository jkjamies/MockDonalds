package com.mockdonalds.app.features.home.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.home.api.ui.HomeTestTags

class HomeUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = HomeStateRobot()

    // --- State + Content ---

    fun setDefaultContent() {
        val state = stateRobot.defaultState()
        rule.setContent { MockDonaldsTheme { HomeUi(state = state) } }
    }

    fun setContentWithNoPromotion() {
        val state = stateRobot.stateWithNoPromotion()
        rule.setContent { MockDonaldsTheme { HomeUi(state = state) } }
    }

    fun setContentWithEmptyCravings() {
        val state = stateRobot.stateWithEmptyCravings()
        rule.setContent { MockDonaldsTheme { HomeUi(state = state) } }
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertUserNameDisplayed("TestUser")
        assertHeroBannerDisplayed()
        assertRecentCravingsDisplayed()
        assertCravingCardDisplayed("1")
        assertExploreSectionDisplayed()
        assertExploreItemDisplayed("1")
        assertExploreItemDisplayed("2")
    }

    fun assertScreenWithNoPromotion() {
        assertUserNameDisplayed("TestUser")
        assertHeroBannerNotDisplayed()
        assertRecentCravingsDisplayed()
        assertExploreSectionDisplayed()
    }

    fun assertScreenWithEmptyCravings() {
        assertUserNameDisplayed("TestUser")
        assertHeroBannerDisplayed()
        assertRecentCravingsNotDisplayed()
        assertExploreSectionDisplayed()
    }

    // --- Element Assertions ---

    private fun assertUserNameDisplayed(name: String) {
        rule.onNodeWithTag(HomeTestTags.USER_NAME).assertIsDisplayed()
        rule.onNodeWithText(name).assertIsDisplayed()
    }

    private fun assertHeroBannerDisplayed() {
        rule.onNodeWithTag(HomeTestTags.HERO_BANNER).assertIsDisplayed()
    }

    private fun assertHeroBannerNotDisplayed() {
        rule.onNodeWithTag(HomeTestTags.HERO_BANNER).assertDoesNotExist()
    }

    private fun assertRecentCravingsDisplayed() {
        rule.onNodeWithTag(HomeTestTags.RECENT_CRAVINGS_SECTION).assertIsDisplayed()
    }

    private fun assertRecentCravingsNotDisplayed() {
        rule.onNodeWithTag(HomeTestTags.RECENT_CRAVINGS_SECTION).assertDoesNotExist()
    }

    private fun assertExploreSectionDisplayed() {
        rule.onNodeWithTag(HomeTestTags.EXPLORE_SECTION).performScrollTo().assertIsDisplayed()
    }

    private fun assertCravingCardDisplayed(id: String) {
        rule.onNodeWithTag("${HomeTestTags.CRAVING_CARD}-$id").assertIsDisplayed()
    }

    private fun assertExploreItemDisplayed(id: String) {
        rule.onNodeWithTag("${HomeTestTags.EXPLORE_ITEM}-$id").performScrollTo().assertIsDisplayed()
    }

    // --- Actions ---

    fun tapHeroCtaButton() {
        rule.onNodeWithTag(HomeTestTags.HERO_CTA_BUTTON).performClick()
    }

    fun tapCravingCard(id: String) {
        rule.onNodeWithTag("${HomeTestTags.CRAVING_CARD}-$id").performClick()
    }

    fun tapExploreItem(id: String) {
        rule.onNodeWithTag("${HomeTestTags.EXPLORE_ITEM}-$id").performScrollTo().performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: HomeEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
