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

    fun setDefaultContent() {
        setContentWith(stateRobot.defaultState())
    }

    fun setContentWithNoCart() {
        setContentWith(stateRobot.stateWithNoCart())
    }

    fun setLandscapeContent() {
        setContentWith(stateRobot.defaultState(), landscape = true)
    }

    fun assertDefaultScreen() {
        assertCategoryPreviewDisplayed("burgers")
        assertCategoryPreviewDisplayed("drinks")
        assertCartBarDisplayed()
        assertCartItemCount(2)
    }

    // OrderUi has no orientation branch — there is no compact-height layout to distinguish
    // from the default one. What this guards is that the screen survives a landscape window:
    // a fixed height or a non-scrolling container would push the cart bar off-screen and fail
    // here. If OrderUi ever grows an `isLandscape` branch, this must assert what differs.
    fun assertLandscapeScreen() {
        assertCategoryPreviewDisplayed("burgers")
        assertCategoryPreviewDisplayed("drinks")
        assertCartBarDisplayed()
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
