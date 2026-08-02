import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct OrderViewTest {

    private let robot = OrderViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersWithNoCart() throws {
        try robot.assertScreenWithNoCart()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    // MARK: - Events

    @Test func categoryTapEmitsEvent() {
        robot.simulateCategoryTap(id: "burgers")
        robot.assertLastEvent(OrderEvent.CategoryTapped(id: "burgers"))
    }

    @Test func cartTapEmitsEvent() {
        robot.simulateCartTap()
        robot.assertLastEvent(OrderEvent.CartClicked())
    }
}
