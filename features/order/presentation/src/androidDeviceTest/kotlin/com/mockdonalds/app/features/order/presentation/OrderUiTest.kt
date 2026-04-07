package com.mockdonalds.app.features.order.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class OrderUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { OrderUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersWithNoCart() {
        robot.setContentWithNoCart()
        robot.assertScreenWithNoCart()
    }

    @Test
    fun categoryChipEmitsEvent() {
        robot.setDefaultContent()
        robot.tapCategoryChip("2")
        robot.assertLastEvent(OrderEvent.CategorySelected("2"))
    }

    @Test
    fun addToOrderEmitsEvent() {
        robot.setDefaultContent()
        robot.tapAddToOrder("1")
        robot.assertLastEvent(OrderEvent.AddToOrder("1"))
    }

    @Test
    fun cartBarEmitsEvent() {
        robot.setDefaultContent()
        robot.tapCartBar()
        robot.assertLastEvent(OrderEvent.CartClicked)
    }
}
