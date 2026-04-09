import XCTest

/// Deep link journey — cold start with deep link URIs and verify
/// the correct screens are reached through the navigation stack.
final class DeepLinkJourneyTest: XCTestCase {

    private let robot = AppRobot()

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testDeepLinkToOrderScreen() {
        robot.launchWithDeepLink("mockdonalds://app/order")
        robot.assertElementDisplayed("OrderFeaturedItemsSection")
    }

    func testDeepLinkToMoreScreen() {
        robot.launchWithDeepLink("mockdonalds://app/more")
        robot.assertElementDisplayed("MoreMenuList")
    }

    func testDeepLinkToProfileRedirectsToLoginWhenUnauthenticated() {
        robot.launchWithDeepLink("mockdonalds://app/more/profile")

        // Auth interception should redirect to login since user is not authenticated
        robot.assertElementNotDisplayed("ProfileAvatar")
        robot.assertElementDisplayed("LoginBranding")
    }
}
