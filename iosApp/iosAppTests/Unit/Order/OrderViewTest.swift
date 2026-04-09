import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct OrderViewTest {

    private let robot = OrderViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    @Test func rendersWithNoCart() throws {
        try robot.assertScreenWithNoCart()
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
