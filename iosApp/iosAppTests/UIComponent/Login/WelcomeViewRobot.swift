import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class WelcomeViewRobot {

    private let stateRobot = WelcomeStateRobot()
    private let tags = WelcomeTestTags.shared

    // MARK: - State + View Creation

    func createDefaultView() -> WelcomeView {
        WelcomeView(state: stateRobot.defaultState())
    }

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.LOGO)
        try body.find(viewWithAccessibilityIdentifier: tags.TITLE)
        try body.find(viewWithAccessibilityIdentifier: tags.SUBTITLE)
        try body.find(viewWithAccessibilityIdentifier: tags.CONTINUE_BUTTON)
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.LOGO)
        try body.find(viewWithAccessibilityIdentifier: tags.TITLE)
        try body.find(viewWithAccessibilityIdentifier: tags.SUBTITLE)
        try body.find(viewWithAccessibilityIdentifier: tags.CONTINUE_BUTTON)
    }

    // MARK: - Event Verification

    func simulateContinueTap() {
        let state = stateRobot.defaultState()
        state.eventSink(WelcomeEvent.ContinueClicked())
    }

    func assertLastEvent(_ expected: WelcomeEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
