import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class ProfileViewRobot {

    private let stateRobot = ProfileStateRobot()
    private let tags = ProfileTestTags.shared

    // MARK: - State + View Creation

    func createDefaultView() -> ProfileView {
        ProfileView(state: stateRobot.defaultState())
    }

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.AVATAR)
        try body.find(viewWithAccessibilityIdentifier: tags.NAME)
        try body.find(viewWithAccessibilityIdentifier: tags.EMAIL)
        try body.find(viewWithAccessibilityIdentifier: tags.TIER_POINTS)
        try body.find(viewWithAccessibilityIdentifier: tags.MEMBER_SINCE)
        try body.find(viewWithAccessibilityIdentifier: tags.LOGOUT_BUTTON)
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.NAME)
        try body.find(viewWithAccessibilityIdentifier: tags.EMAIL)
        try body.find(viewWithAccessibilityIdentifier: tags.LOGOUT_BUTTON)
    }

    // MARK: - Event Verification

    func simulateLogoutTap() {
        let state = stateRobot.defaultState()
        state.eventSink(ProfileEvent.LogoutClicked())
    }

    func assertLastEvent(_ expected: ProfileEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
