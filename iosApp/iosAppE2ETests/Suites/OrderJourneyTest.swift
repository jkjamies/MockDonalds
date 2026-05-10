import XCTest

/// Order journey — navigate to order, browse categories, and verify the
/// vertical category-list layout renders correctly.
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
        robot.assertElementDisplayed("OrderCategoryPreviewCard-burgers")
    }

    func testReturnToHomeFromOrder() {
        robot.tapTab("ORDER")
        robot.assertElementDisplayed("OrderCategoryPreviewCard-burgers")

        robot.tapTab("HOME")
        robot.assertElementDisplayed("HomeUserName")
    }
}
