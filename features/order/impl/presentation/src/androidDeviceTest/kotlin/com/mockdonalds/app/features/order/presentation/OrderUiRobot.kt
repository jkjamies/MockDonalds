package com.mockdonalds.app.features.order.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.order.api.ui.OrderTestTags

class OrderUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = OrderStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: OrderUiState) {
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp)),
            ) {
                MockDonaldsTheme { OrderUi(state = state) }
            }
        }
    }

    fun setDefaultContent() {
        setContentWith(stateRobot.defaultState())
    }

    fun setContentWithNoCart() {
        setContentWith(stateRobot.stateWithNoCart())
    }

    fun assertDefaultScreen() {
        assertCategoryPreviewDisplayed("burgers")
        assertCategoryPreviewDisplayed("drinks")
        assertCartBarDisplayed()
        assertCartItemCount(2)
    }

    fun assertScreenWithNoCart() {
        assertCategoryPreviewDisplayed("burgers")
        assertCartBarNotDisplayed()
    }

    private fun assertCategoryPreviewDisplayed(id: String) {
        rule.onNodeWithTag("${OrderTestTags.CATEGORY_PREVIEW_CARD}-$id").assertIsDisplayed()
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

    fun tapCategoryPreview(id: String) {
        rule.onNodeWithTag("${OrderTestTags.CATEGORY_PREVIEW_CARD}-$id").performClick()
    }

    fun tapCartBar() {
        rule.onNodeWithTag(OrderTestTags.CART_BAR).performClick()
    }

    fun assertLastEvent(expected: OrderEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
