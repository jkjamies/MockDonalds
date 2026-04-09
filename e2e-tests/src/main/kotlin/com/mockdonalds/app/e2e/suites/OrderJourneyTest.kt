package com.mockdonalds.app.e2e.suites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mockdonalds.app.e2e.robots.AppRobot
import com.mockdonalds.app.features.home.api.ui.HomeTestTags
import com.mockdonalds.app.features.order.api.ui.OrderTestTags
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Order journey — navigate from home to order screen,
 * browse featured items, and verify the order flow.
 */
@RunWith(AndroidJUnit4::class)
class OrderJourneyTest {

    private val robot = AppRobot()

    @Before
    fun setUp() {
        robot.launchApp()
    }

    @Test
    fun navigateFromHomeToOrder() {
        robot.assertElementDisplayed(HomeTestTags.USER_NAME)

        robot.tapTab("Order")
        robot.assertElementDisplayed(OrderTestTags.FEATURED_ITEMS_SECTION)
    }

    @Test
    fun orderScreenShowsFeaturedItems() {
        robot.tapTab("Order")
        robot.assertElementDisplayed(OrderTestTags.FEATURED_ITEMS_SECTION)
    }

    @Test
    fun returnToHomeFromOrder() {
        robot.tapTab("Order")
        robot.assertElementDisplayed(OrderTestTags.FEATURED_ITEMS_SECTION)

        robot.tapTab("Home")
        robot.assertElementDisplayed(HomeTestTags.USER_NAME)
    }
}
