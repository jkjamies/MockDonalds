import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct WelcomeViewTest {

    private let robot = WelcomeViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    // MARK: - Events

    @Test func continueButtonEmitsEvent() {
        robot.simulateContinueTap()
        robot.assertLastEvent(WelcomeEvent.ContinueClicked())
    }
}
