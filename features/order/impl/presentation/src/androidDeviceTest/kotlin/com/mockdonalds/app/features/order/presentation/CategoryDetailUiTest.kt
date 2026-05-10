package com.mockdonalds.app.features.order.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class CategoryDetailUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { CategoryDetailUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersWithNoItems() {
        robot.setContentWithNoItems()
        robot.assertEmptyItemsScreen()
    }

    @Test
    fun rendersWithNoCart() {
        robot.setContentWithNoCart()
        robot.assertNoCartScreen()
    }

    @Test
    fun backButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapBack()
        robot.assertLastEvent(CategoryDetailEvent.BackPressed)
    }

    @Test
    fun addToOrderEmitsEvent() {
        robot.setDefaultContent()
        robot.tapAddToOrder("1")
        robot.assertLastEvent(CategoryDetailEvent.AddToOrder("1"))
    }

    @Test
    fun cartBarEmitsEvent() {
        robot.setDefaultContent()
        robot.tapCartBar()
        robot.assertLastEvent(CategoryDetailEvent.CartClicked)
    }
}
