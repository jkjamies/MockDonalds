import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct RecentsViewTest {

    private let robot = RecentsViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLoadingState() throws {
        try robot.assertLoadingScreen()
    }

    @Test func rendersEmptyState() throws {
        try robot.assertEmptyScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    // MARK: - Events

    @Test func backTapEmitsEvent() {
        robot.simulateBackTap()
        robot.assertLastEvent(RecentsEventOnBackTapped())
    }

    @Test func itemTapEmitsEvent() {
        robot.simulateItemTap(id: "1")
        robot.assertLastEvent(RecentsEventOnItemTapped(id: "1"))
    }
}
