import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct LoginViewTest {

    private let robot = LoginViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    @Test func rendersWithEmail() throws {
        try robot.assertViewWithEmail()
    }

    // MARK: - Events

    @Test func signInConfirmEmitsEvent() {
        robot.simulateSignInConfirm()
        robot.assertLastEvent(LoginEvent.SignInConfirmed())
    }

    @Test func appleSignInTapEmitsEvent() {
        robot.simulateAppleSignInTap()
        robot.assertLastEvent(LoginEvent.AppleSignInClicked())
    }

    @Test func googleSignInTapEmitsEvent() {
        robot.simulateGoogleSignInTap()
        robot.assertLastEvent(LoginEvent.GoogleSignInClicked())
    }
}
