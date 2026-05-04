package com.mockdonalds.app.features.kiosk.order.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.kiosk.order.api.ui.KioskOrderTestTags

class KioskOrderUiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = KioskOrderStateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: KioskOrderUiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(960.dp, 540.dp) else DpSize(540.dp, 960.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { KioskOrderUi(state = state) }
            }
        }
    }

    fun setDefaultContent() = setContentWith(stateRobot.defaultState())
    fun setLandscapeContent() = setContentWith(stateRobot.defaultState(), landscape = true)
    fun setEmptyCartContent() = setContentWith(stateRobot.emptyCartState())
    fun setCartWithItemsContent() = setContentWith(stateRobot.cartWithItemsState())

    fun assertDefaultScreen() {
        rule.onNodeWithTag(KioskOrderTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.NAV_RAIL).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.NAV_RAIL_HOME).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.ITEM_GRID).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.CART_BAR).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.CART_TOTAL).assertIsDisplayed()
    }

    fun assertCartTotal() {
        rule.onNodeWithTag(KioskOrderTestTags.CART_TOTAL).assertIsDisplayed()
    }

    fun assertLandscapeScreen() {
        rule.onNodeWithTag(KioskOrderTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.NAV_RAIL).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.CART_BAR).assertIsDisplayed()
    }

    fun tapBack() {
        rule.onNodeWithTag(KioskOrderTestTags.BACK_BUTTON).performClick()
    }

    fun tapCancel() {
        rule.onNodeWithTag(KioskOrderTestTags.CANCEL_BUTTON).performClick()
    }

    fun tapCategory(id: String) {
        rule.onNodeWithTag("${KioskOrderTestTags.NAV_RAIL_CATEGORY_PREFIX}$id").performClick()
    }

    fun tapItem(id: String) {
        rule.onNodeWithTag("${KioskOrderTestTags.ITEM_CARD_PREFIX}$id").performClick()
    }

    fun assertLastEvent(expected: KioskOrderEvent) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
