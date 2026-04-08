import Testing
import ComposeApp
@testable import iosApp

@Suite struct LoginViewTest {

    private let robot = LoginViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    @Test func rendersWithEmail() {
        robot.assertViewWithEmailCreated()
    }

    // MARK: - Events

    @Test func signInTapEmitsEvent() {
        robot.simulateSignInTap()
        robot.assertLastEvent(LoginEvent.SignInClicked())
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
