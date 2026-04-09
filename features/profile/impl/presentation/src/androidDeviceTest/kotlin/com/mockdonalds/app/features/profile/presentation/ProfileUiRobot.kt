package com.mockdonalds.app.features.profile.presentation

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
import com.mockdonalds.app.features.profile.api.ui.ProfileTestTags

class ProfileUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = ProfileStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: ProfileUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { ProfileUi(state = state) }
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

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertNameDisplayed("Night Owl")
        assertEmailDisplayed()
        assertTierPointsDisplayed()
        assertMemberSinceDisplayed()
        assertLogoutButtonDisplayed()
    }

    fun assertLandscapeScreen() {
        assertNameDisplayed("Night Owl")
        assertEmailDisplayed()
        assertLogoutButtonDisplayed()
    }

    // --- Element Assertions ---

    private fun assertNameDisplayed(name: String) {
        rule.onNodeWithTag(ProfileTestTags.NAME).assertIsDisplayed()
        rule.onNodeWithText(name).assertIsDisplayed()
    }

    private fun assertEmailDisplayed() {
        rule.onNodeWithTag(ProfileTestTags.EMAIL).assertIsDisplayed()
    }

    private fun assertTierPointsDisplayed() {
        rule.onNodeWithTag(ProfileTestTags.TIER_POINTS).assertIsDisplayed()
    }

    private fun assertMemberSinceDisplayed() {
        rule.onNodeWithTag(ProfileTestTags.MEMBER_SINCE).assertIsDisplayed()
    }

    private fun assertLogoutButtonDisplayed() {
        rule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performScrollTo().assertIsDisplayed()
    }

    // --- Actions ---

    fun tapLogoutButton() {
        rule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performScrollTo().performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: ProfileEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
