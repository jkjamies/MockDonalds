import SwiftUI
import Testing
import ComposeApp
@testable import iosApp

final class LoginViewRobot {

    private let stateRobot = LoginStateRobot()

    // MARK: - State + View Creation

    func createDefaultView() -> LoginView {
        LoginView(state: stateRobot.defaultState())
    }

    func createViewWithEmail(
        _ email: String
    ) -> LoginView {
        LoginView(
            state: stateRobot.stateWithEmail(email)
        )
    }

    // MARK: - Screen Assertions

    func assertDefaultViewCreated() {
        let view = createDefaultView()
        #expect(view.body != nil)
    }

    func assertViewWithEmailCreated() {
        let view = createViewWithEmail("test@example.com")
        #expect(view.body != nil)
    }

    // MARK: - Event Verification

    func simulateSignInTap() {
        let state = stateRobot.defaultState()
        state.eventSink(LoginEvent.SignInClicked())
    }

    func simulateForgotPasswordTap() {
        let state = stateRobot.defaultState()
        state.eventSink(LoginEvent.ForgotPasswordClicked())
    }

    func simulateAppleSignInTap() {
        let state = stateRobot.defaultState()
        state.eventSink(LoginEvent.AppleSignInClicked())
    }

    func simulateGoogleSignInTap() {
        let state = stateRobot.defaultState()
        state.eventSink(LoginEvent.GoogleSignInClicked())
    }

    func simulateSignUpTap() {
        let state = stateRobot.defaultState()
        state.eventSink(LoginEvent.SignUpClicked())
    }

    func assertLastEvent(_ expected: LoginEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
