package com.mockdonalds.app.e2e.suites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mockdonalds.app.e2e.robots.AppRobot
import com.mockdonalds.app.features.home.api.ui.HomeTestTags
import com.mockdonalds.app.features.order.api.ui.CategoryDetailTestTags
import com.mockdonalds.app.features.order.api.ui.OrderTestTags
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Order journey — navigate from home to the order tab and verify
 * the vertical category-list layout renders correctly.
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
        robot.assertElementDisplayed("${OrderTestTags.CATEGORY_PREVIEW_CARD}-burgers")
    }

    @Test
    fun orderScreenShowsCategoryPreviews() {
        robot.tapTab("Order")
        robot.assertElementDisplayed("${OrderTestTags.CATEGORY_PREVIEW_CARD}-burgers")
    }

    @Test
    fun returnToHomeFromOrder() {
        robot.tapTab("Order")
        robot.assertElementDisplayed("${OrderTestTags.CATEGORY_PREVIEW_CARD}-burgers")

        robot.tapTab("Home")
        robot.assertElementDisplayed(HomeTestTags.USER_NAME)
    }

    @Test
    fun tappingCategoryOpensDetailScreen() {
        robot.tapTab("Order")
        robot.tapElement("${OrderTestTags.CATEGORY_PREVIEW_CARD}-burgers")
        robot.assertElementDisplayed(CategoryDetailTestTags.TOP_BAR)
    }
}
