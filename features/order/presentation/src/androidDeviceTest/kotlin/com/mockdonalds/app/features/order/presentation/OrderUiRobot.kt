package com.mockdonalds.app.features.order.presentation

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
import com.mockdonalds.app.features.order.api.ui.OrderTestTags

class OrderUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = OrderStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: OrderUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { OrderUi(state = state) }
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

    fun setContentWithNoCart() {
        setContentWith(stateRobot.stateWithNoCart())
    }

    // --- Screen Assertions ---

    fun assertDefaultScreen() {
        assertCategoryChipDisplayed("1")
        assertCategoryChipDisplayed("2")
        assertFeaturedItemsSectionDisplayed()
        assertFeaturedItemCardDisplayed("1")
        assertCartBarDisplayed()
        assertCartItemCount(2)
    }

    fun assertLandscapeScreen() {
        assertCategoryChipDisplayed("1")
        assertFeaturedItemsSectionDisplayed()
        assertFeaturedItemCardDisplayed("1")
        assertCartBarDisplayed()
    }

    fun assertScreenWithNoCart() {
        assertCategoryChipDisplayed("1")
        assertFeaturedItemsSectionDisplayed()
        assertCartBarNotDisplayed()
    }

    // --- Element Assertions ---

    private fun assertCategoryChipDisplayed(id: String) {
        rule.onNodeWithTag("${OrderTestTags.CATEGORY_CHIP}-$id").assertIsDisplayed()
    }

    private fun assertFeaturedItemsSectionDisplayed() {
        rule.onNodeWithTag(OrderTestTags.FEATURED_ITEMS_SECTION).assertIsDisplayed()
    }

    private fun assertFeaturedItemCardDisplayed(id: String) {
        rule.onNodeWithTag("${OrderTestTags.FEATURED_ITEM_CARD}-$id").assertIsDisplayed()
    }

    private fun assertCartBarDisplayed() {
        rule.onNodeWithTag(OrderTestTags.CART_BAR).assertIsDisplayed()
    }

    private fun assertCartBarNotDisplayed() {
        rule.onNodeWithTag(OrderTestTags.CART_BAR).assertDoesNotExist()
    }

    private fun assertCartItemCount(count: Int) {
        rule.onNodeWithText("$count ITEMS").assertIsDisplayed()
    }

    // --- Actions ---

    fun tapCategoryChip(id: String) {
        rule.onNodeWithTag("${OrderTestTags.CATEGORY_CHIP}-$id").performClick()
    }

    fun tapAddToOrder(itemId: String) {
        rule.onNodeWithTag("${OrderTestTags.ADD_TO_ORDER_BUTTON}-$itemId").performScrollTo().performClick()
    }

    fun tapCartBar() {
        rule.onNodeWithTag(OrderTestTags.CART_BAR).performClick()
    }

    // --- Event Verification ---

    fun assertLastEvent(expected: OrderEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
