package com.mockdonalds.app.features.kiosk.order.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class KioskOrderUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { KioskOrderUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultScreen() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersEmptyCart() {
        robot.setEmptyCartContent()
        robot.assertCartTotal()
    }

    @Test
    fun rendersCartWithItems() {
        robot.setCartWithItemsContent()
        robot.assertCartTotal()
    }

    @Test
    fun rendersLandscapeLayout() {
        robot.setLandscapeContent()
        robot.assertLandscapeScreen()
    }

    @Test
    fun backButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapBack()
        robot.assertLastEvent(KioskOrderEvent.BackPressed)
    }

    @Test
    fun cancelButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapCancel()
        robot.assertLastEvent(KioskOrderEvent.CancelOrderPressed)
    }
}
