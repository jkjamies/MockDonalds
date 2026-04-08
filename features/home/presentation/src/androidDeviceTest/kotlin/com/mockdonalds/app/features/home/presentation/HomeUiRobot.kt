package com.mockdonalds.app.features.home.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.home.api.ui.HomeTestTags

class HomeUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = HomeStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: HomeUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { HomeUi(state = state) }
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

    fun setContentWithNoPromotion() {
        setContentWith(stateRobot.stateWithNoPromotion())
    }

    fun setContentWithEmptyCravings() {
        setContentWith(stateRobot.stateWithEmptyCravings())
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

    fun assertLandscapeScreen() {
        assertUserNameDisplayed("TestUser")
        assertHeroBannerDisplayed()
        assertRecentCravingsDisplayed()
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
