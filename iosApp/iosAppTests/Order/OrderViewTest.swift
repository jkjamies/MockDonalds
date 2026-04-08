import Testing
import ComposeApp
@testable import iosApp

@Suite struct OrderViewTest {

    private let robot = OrderViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    @Test func rendersWithNoCart() {
        robot.assertViewWithNoCartCreated()
    }

    // MARK: - Events

    @Test func categoryTapEmitsEvent() {
        robot.simulateCategoryTap(id: "1")
        robot.assertLastEvent(OrderEvent.CategorySelected(id: "1"))
    }

    @Test func addToOrderEmitsEvent() {
        robot.simulateAddToOrder(itemId: "1")
        robot.assertLastEvent(OrderEvent.AddToOrder(itemId: "1"))
    }

    @Test func cartTapEmitsEvent() {
        robot.simulateCartTap()
        robot.assertLastEvent(OrderEvent.CartClicked())
    }
}
