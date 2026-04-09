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

    func testNavigateToOrderAndBrowse() {
        robot.tapTab("Order")
        robot.assertElementDisplayed("OrderFeaturedItemsSection")
        robot.assertElementDisplayed("OrderCategoryChip")
    }

    func testOrderScreenShowsFeaturedItems() {
        robot.tapTab("Order")
        robot.assertElementDisplayed("OrderFeaturedItemCard")
    }
}
