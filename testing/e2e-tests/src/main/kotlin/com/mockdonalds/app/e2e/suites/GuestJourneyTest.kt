package com.mockdonalds.app.e2e.suites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mockdonalds.app.e2e.robots.AppRobot
import com.mockdonalds.app.features.home.api.ui.HomeTestTags
import com.mockdonalds.app.features.login.api.ui.LoginTestTags
import com.mockdonalds.app.features.more.api.ui.MoreTestTags
import com.mockdonalds.app.features.order.api.ui.OrderTestTags
import com.mockdonalds.app.features.rewards.api.ui.RewardsTestTags
import com.mockdonalds.app.features.scan.api.ui.ScanTestTags
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Guest user journey — browse the app without authenticating.
 * Verifies core screens render, tab navigation works, and
 * auth-gated features redirect to login.
 */
@RunWith(AndroidJUnit4::class)
class GuestJourneyTest {

    private val robot = AppRobot()

    @Before
    fun setUp() {
        robot.launchApp()
    }

    @Test
    fun homeScreenRendersOnLaunch() {
        robot.assertElementDisplayed(HomeTestTags.USER_NAME)
        robot.assertElementDisplayed(HomeTestTags.HERO_BANNER)
    }

    @Test
    fun browseAllTabs() {
        // Home is default
        robot.assertElementDisplayed(HomeTestTags.USER_NAME)

        // Order tab
        robot.tapTab("Order")
        robot.assertElementDisplayed(OrderTestTags.FEATURED_ITEMS_SECTION)

        // Rewards tab
        robot.tapTab("Rewards")
        robot.assertElementDisplayed(RewardsTestTags.POINTS_SECTION)

        // Scan tab
        robot.tapTab("Scan")
        robot.assertElementDisplayed(ScanTestTags.SCAN_BUTTON)

        // More tab
        robot.tapTab("More")
        robot.assertElementDisplayed(MoreTestTags.MENU_LIST)

        // Back to home
        robot.tapTab("Home")
        robot.assertElementDisplayed(HomeTestTags.USER_NAME)
    }

    @Test
    fun profileNavigationRedirectsToLoginWhenUnauthenticated() {
        robot.tapTab("More")
        robot.assertElementDisplayed(MoreTestTags.PROFILE_SECTION)
        robot.tapElement(MoreTestTags.PROFILE_SECTION)

        // Auth interception should redirect to login
        robot.assertElementDisplayed(LoginTestTags.BRANDING)
        robot.assertElementDisplayed(LoginTestTags.SIGN_IN_BUTTON)
    }
}
