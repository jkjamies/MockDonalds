package com.jkjamies.sampleplatter.features.debugmenu.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.jkjamies.sampleplatter.core.theme.LocalWindowSizeClass
import com.jkjamies.sampleplatter.core.theme.SamplePlatterTheme
import com.jkjamies.sampleplatter.features.debugmenu.api.ui.DebugMenuTestTags

class DebugMenuUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = DebugMenuStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: DebugMenuUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                SamplePlatterTheme { DebugMenuUi(state = state) }
            }
        }
    }

    // --- State + Content ---

    fun setDefaultContent() {
        setContentWith(stateRobot.defaultState())
    }

    fun setContentWithNoEntries() {
        setContentWith(stateRobot.stateWithNoEntries())
    }

    fun setLandscapeContent() {
        setContentWith(stateRobot.defaultState(), landscape = true)
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertRootDisplayed()
        assertEntryListDisplayed()
        assertEntryDisplayed("feature-flags")
        assertEntryDisplayed("build-config")
    }

    fun assertScreenWithNoEntries() {
        assertRootDisplayed()
        assertEntryListDisplayed()
    }

    fun assertLandscapeScreen() {
        assertRootDisplayed()
        assertEntryListDisplayed()
        assertEntryDisplayed("feature-flags")
        assertEntryDisplayed("build-config")
    }

    // --- Element Assertions ---

    private fun assertRootDisplayed() {
        rule.onNodeWithTag(DebugMenuTestTags.ROOT).assertIsDisplayed()
    }

    private fun assertEntryListDisplayed() {
        rule.onNodeWithTag(DebugMenuTestTags.ENTRY_LIST).assertIsDisplayed()
    }

    private fun assertEntryDisplayed(id: String) {
        rule.onNodeWithTag("${DebugMenuTestTags.ENTRY_ITEM}-$id").assertIsDisplayed()
    }

    // --- Actions ---

    fun tapEntry(id: String) {
        rule.onNodeWithTag("${DebugMenuTestTags.ENTRY_ITEM}-$id").performClick()
    }

    fun tapBack() {
        rule.onNodeWithTag(DebugMenuTestTags.BACK_BUTTON).performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: DebugMenuEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
