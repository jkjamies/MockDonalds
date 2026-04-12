package com.mockdonalds.app.e2e.suites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mockdonalds.app.e2e.robots.AppRobot
import com.mockdonalds.app.features.login.api.ui.LoginTestTags
import com.mockdonalds.app.features.login.api.ui.WelcomeTestTags
import com.mockdonalds.app.features.more.api.ui.MoreTestTags
import com.mockdonalds.app.features.order.api.ui.OrderTestTags
import com.mockdonalds.app.features.profile.api.ui.ProfileTestTags
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Deep link journey — cold start with deep link URIs and verify
 * the correct screens are reached through the navigation stack.
 */
@RunWith(AndroidJUnit4::class)
class DeepLinkJourneyTest {

    private val robot = AppRobot()

    @Test
    fun deepLinkToOrderScreen() {
        robot.launchWithDeepLink("mockdonalds://app/order")
        robot.assertElementDisplayed(OrderTestTags.FEATURED_ITEMS_SECTION)
    }

    @Test
    fun deepLinkToMoreScreen() {
        robot.launchWithDeepLink("mockdonalds://app/more")
        robot.assertElementDisplayed(MoreTestTags.MENU_LIST)
    }

    @Test
    fun deepLinkToProfileRedirectsToLoginWhenUnauthenticated() {
        robot.launchWithDeepLink("mockdonalds://app/more/profile")

        // Auth interception should redirect to login since user is not authenticated
        // Profile screen (ProfileTestTags.AVATAR, NAME, etc.) requires authentication
        robot.assertElementNotDisplayed(ProfileTestTags.AVATAR)
        robot.assertElementDisplayed(LoginTestTags.BRANDING)
    }

    @Test
    fun deepLinkToProfileSignInFlowShowsWelcomeScreen() {
        robot.launchWithDeepLink("mockdonalds://app/more/profile")

        // Login screen appears via auth interception
        robot.assertElementDisplayed(LoginTestTags.SIGN_IN_BUTTON)

        // Trigger sign-in flow
        robot.tapElement(LoginTestTags.SIGN_IN_BUTTON)
        robot.tapText("Send Link")

        // WelcomeScreen should appear within the flow
        robot.assertElementDisplayed(WelcomeTestTags.LOGO)
        robot.assertElementDisplayed(WelcomeTestTags.TITLE)
        robot.assertElementDisplayed(WelcomeTestTags.CONTINUE_BUTTON)
    }
}
