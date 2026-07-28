package com.jkjamies.sampleplatter.features.order.presentation

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
    fun rendersLandscapeLayout() {
        robot.setLandscapeContent()
        robot.assertLandscapeScreen()
    }

    @Test
    fun categoryPreviewEmitsEvent() {
        robot.setDefaultContent()
        robot.tapCategoryPreview("drinks")
        robot.assertLastEvent(OrderEvent.CategoryTapped("drinks"))
    }

    @Test
    fun cartBarEmitsEvent() {
        robot.setDefaultContent()
        robot.tapCartBar()
        robot.assertLastEvent(OrderEvent.CartClicked)
    }
}
