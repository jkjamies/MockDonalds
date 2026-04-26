import XCTest

/// Order journey — navigate to order, browse categories, and interact
/// with featured items. Verifies the ordering flow renders correctly.
final class OrderJourneyTest: XCTestCase {

    private let robot = AppRobot()

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
        robot.launchApp()
    }

    func testNavigateFromHomeToOrder() {
        robot.assertElementDisplayed("HomeUserName")

        robot.tapTab("ORDER")
        robot.assertElementDisplayed("OrderFeaturedItemsSection")
    }

    func testReturnToHomeFromOrder() {
        robot.tapTab("ORDER")
        robot.assertElementDisplayed("OrderFeaturedItemsSection")

        robot.tapTab("HOME")
        robot.assertElementDisplayed("HomeUserName")
    }
}
