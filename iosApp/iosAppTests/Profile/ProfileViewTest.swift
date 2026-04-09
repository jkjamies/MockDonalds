import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct ProfileViewTest {

    private let robot = ProfileViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    // MARK: - Events

    @Test func logoutButtonEmitsEvent() {
        robot.simulateLogoutTap()
        robot.assertLastEvent(ProfileEvent.LogoutClicked())
    }
}
