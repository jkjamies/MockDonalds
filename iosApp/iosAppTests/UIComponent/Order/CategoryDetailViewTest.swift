import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct CategoryDetailViewTest {

    private let robot = CategoryDetailViewRobot()

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersWithNoItems() throws {
        try robot.assertEmptyScreen()
    }

    @Test func backButtonEmitsEvent() {
        robot.simulateBackTap()
        robot.assertLastEvent(CategoryDetailEvent.BackPressed())
    }

    @Test func addToOrderEmitsEvent() {
        robot.simulateAddToOrder(itemId: "1")
        robot.assertLastEvent(CategoryDetailEvent.AddToOrder(itemId: "1"))
    }

    @Test func cartTapEmitsEvent() {
        robot.simulateCartTap()
        robot.assertLastEvent(CategoryDetailEvent.CartClicked())
    }
}
