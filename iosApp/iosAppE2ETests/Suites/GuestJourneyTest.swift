import XCTest

/// Guest user journey — browse the app without authenticating.
/// Verifies core screens render, tab navigation works, and
/// auth-gated features redirect to login.
final class GuestJourneyTest: XCTestCase {

    private let robot = AppRobot()

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
        robot.launchApp()
    }

    func testHomeScreenRendersOnLaunch() {
        robot.assertElementDisplayed("HomeUserName")
        robot.assertElementDisplayed("HomeHeroBanner")
    }

    func testBrowseAllTabs() {
        // Home is default
        robot.assertElementDisplayed("HomeUserName")

        // Order tab
        robot.tapTab("ORDER")
        robot.assertElementDisplayed("OrderFeaturedItemsSection")

        // Rewards tab
        robot.tapTab("REWARDS")
        robot.assertElementDisplayed("RewardsPointsSection")

        // Scan tab
        robot.tapTab("SCAN")
        robot.assertElementDisplayed("ScanMemberCard")

        // More tab
        robot.tapTab("MORE")
        robot.assertElementDisplayed("MoreMenuList")

        // Back to home
        robot.tapTab("HOME")
        robot.assertElementDisplayed("HomeUserName")
    }

    func testProfileNavigationRedirectsToLoginWhenUnauthenticated() {
        robot.tapTab("MORE")
        robot.assertElementDisplayed("MoreProfileSection")
        robot.tapElement("MoreProfileSection")

        // Auth interception should redirect to login
        robot.assertElementDisplayed("LoginBranding")
        robot.assertElementDisplayed("LoginSignInButton")
    }

    func testSignInFlowShowsWelcomeScreenThenCompletes() {
        robot.tapTab("MORE")
        robot.tapElement("MoreProfileSection")

        // Login screen appears via auth interception
        robot.assertElementDisplayed("LoginSignInButton")

        // Trigger sign-in flow
        robot.tapElement("LoginSignInButton")
        robot.tapAlertButton("Send Link")

        // WelcomeScreen should appear
        robot.assertElementDisplayed("WelcomeLogo")
        robot.assertElementDisplayed("WelcomeTitle")
        robot.assertElementDisplayed("WelcomeContinueButton")

        // Tap continue to complete flow
        robot.tapElement("WelcomeContinueButton")

        // Flow pops Welcome + Login covers, then navigates to the auth-gated returnTo (Profile)
        robot.assertElementDisplayed("ProfileName")
    }
}
