package com.jkjamies.sampleplatter.features.order.presentation

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
import com.jkjamies.sampleplatter.core.theme.LocalWindowSizeClass
import com.jkjamies.sampleplatter.core.theme.SamplePlatterTheme
import com.jkjamies.sampleplatter.features.order.api.ui.CategoryDetailTestTags

class CategoryDetailUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = CategoryDetailStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: CategoryDetailUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                SamplePlatterTheme { CategoryDetailUi(state = state) }
            }
        }
    }

    fun setDefaultContent() {
        setContentWith(stateRobot.defaultState())
    }

    fun setContentWithNoItems() {
        setContentWith(stateRobot.stateWithNoItems())
    }

    fun setContentWithNoCart() {
        setContentWith(stateRobot.stateWithNoCart())
    }

    fun setLandscapeContent() {
        setContentWith(stateRobot.defaultState(), landscape = true)
    }

    fun assertDefaultScreen() {
        rule.onNodeWithTag(CategoryDetailTestTags.TOP_BAR).assertIsDisplayed()
        rule.onNodeWithTag(CategoryDetailTestTags.BACK_BUTTON).assertIsDisplayed()
        rule.onNodeWithText("Burgers").assertIsDisplayed()
        rule.onNodeWithTag("${CategoryDetailTestTags.MENU_ITEM_CARD}-1").assertIsDisplayed()
        rule.onNodeWithTag(CategoryDetailTestTags.CART_BAR).assertIsDisplayed()
    }

    // CategoryDetailUi has no orientation branch — this guards that the screen survives a
    // landscape window rather than that a distinct compact layout renders. If one is added,
    // this must assert what differs.
    fun assertLandscapeScreen() {
        rule.onNodeWithTag(CategoryDetailTestTags.TOP_BAR).assertIsDisplayed()
        rule.onNodeWithTag(CategoryDetailTestTags.BACK_BUTTON).assertIsDisplayed()
        rule.onNodeWithTag("${CategoryDetailTestTags.MENU_ITEM_CARD}-1").assertIsDisplayed()
        rule.onNodeWithTag(CategoryDetailTestTags.CART_BAR).assertIsDisplayed()
    }

    fun assertEmptyItemsScreen() {
        rule.onNodeWithTag(CategoryDetailTestTags.TOP_BAR).assertIsDisplayed()
        rule.onNodeWithTag("${CategoryDetailTestTags.MENU_ITEM_CARD}-1").assertDoesNotExist()
    }

    fun assertNoCartScreen() {
        rule.onNodeWithTag(CategoryDetailTestTags.TOP_BAR).assertIsDisplayed()
        rule.onNodeWithTag(CategoryDetailTestTags.CART_BAR).assertDoesNotExist()
    }

    fun tapBack() {
        rule.onNodeWithTag(CategoryDetailTestTags.BACK_BUTTON).performClick()
    }

    fun tapAddToOrder(itemId: String) {
        rule.onNodeWithTag("${CategoryDetailTestTags.ADD_TO_ORDER_BUTTON}-$itemId")
            .performScrollTo()
            .performClick()
    }

    fun tapCartBar() {
        rule.onNodeWithTag(CategoryDetailTestTags.CART_BAR).performClick()
    }

    fun assertLastEvent(expected: CategoryDetailEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
