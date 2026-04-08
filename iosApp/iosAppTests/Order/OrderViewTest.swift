import XCTest
import ComposeApp
@testable import iosApp

final class OrderViewTest: XCTestCase {

    private lazy var robot = OrderViewRobot()

    // MARK: - Rendering

    func testRendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    func testRendersWithNoCart() {
        robot.assertViewWithNoCartCreated()
    }

    // MARK: - Events

    func testCategoryTapEmitsEvent() {
        robot.simulateCategoryTap(id: "1")
        robot.assertLastEvent(OrderEvent.CategorySelected(id: "1"))
    }

    func testAddToOrderEmitsEvent() {
        robot.simulateAddToOrder(itemId: "1")
        robot.assertLastEvent(OrderEvent.AddToOrder(itemId: "1"))
    }

    func testCartTapEmitsEvent() {
        robot.simulateCartTap()
        robot.assertLastEvent(OrderEvent.CartClicked())
    }
}
