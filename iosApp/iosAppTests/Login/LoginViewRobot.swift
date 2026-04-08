import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class LoginViewRobot {

    private let stateRobot = LoginStateRobot()
    private let tags = LoginTestTags.shared

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

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.BRANDING)
        try body.find(viewWithAccessibilityIdentifier: tags.EMAIL_INPUT)
        try body.find(viewWithAccessibilityIdentifier: tags.SIGN_IN_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.GOOGLE_BUTTON)
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.BRANDING)
        try body.find(viewWithAccessibilityIdentifier: tags.EMAIL_INPUT)
        try body.find(viewWithAccessibilityIdentifier: tags.SIGN_IN_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.GOOGLE_BUTTON)
    }

    func assertViewWithEmail() throws {
        let view = createViewWithEmail("test@example.com")
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.BRANDING)
        try body.find(viewWithAccessibilityIdentifier: tags.EMAIL_INPUT)
        try body.find(viewWithAccessibilityIdentifier: tags.SIGN_IN_BUTTON)
    }

    // MARK: - Event Verification

    func simulateSignInTap() {
        let state = stateRobot.defaultState()
        state.eventSink(LoginEvent.SignInClicked())
    }

    func simulateAppleSignInTap() {
        let state = stateRobot.defaultState()
        state.eventSink(LoginEvent.AppleSignInClicked())
    }

    func simulateGoogleSignInTap() {
        let state = stateRobot.defaultState()
        state.eventSink(LoginEvent.GoogleSignInClicked())
    }

    func assertLastEvent(_ expected: LoginEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
