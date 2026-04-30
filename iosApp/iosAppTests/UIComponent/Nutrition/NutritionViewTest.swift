import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct NutritionViewTest {

    private let robot = NutritionViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLoadingState() throws {
        try robot.assertLoadingScreen()
    }

    // MARK: - Events

    @Test func backTapEmitsEvent() {
        robot.simulateBackTap()
        robot.assertLastEvent(NutritionEvent.BackClicked())
    }
}
