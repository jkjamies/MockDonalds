import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class MoreViewRobot {

    private let stateRobot = MoreStateRobot()
    private let tags = MoreTestTags.shared

    // MARK: - State + View Creation

    func createDefaultView() -> MoreView {
        MoreView(state: stateRobot.defaultState())
    }

    func createViewWithNoProfile() -> MoreView {
        MoreView(state: stateRobot.stateWithNoProfile())
    }

    func createViewWithEmptyMenu() -> MoreView {
        MoreView(state: stateRobot.stateWithEmptyMenu())
    }

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.PROFILE_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.MENU_LIST)
        try body.find(viewWithAccessibilityIdentifier: "\(tags.MENU_ITEM)-1")
        try body.find(viewWithAccessibilityIdentifier: "\(tags.MENU_ITEM)-2")
        try body.find(viewWithAccessibilityIdentifier: tags.JOIN_TEAM_BANNER)
    }

    func assertScreenWithNoProfile() throws {
        let view = createViewWithNoProfile()
        let body = try view.inspect()
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.PROFILE_SECTION)
        }
        try body.find(viewWithAccessibilityIdentifier: tags.MENU_LIST)
        try body.find(viewWithAccessibilityIdentifier: tags.JOIN_TEAM_BANNER)
    }

    func assertScreenWithEmptyMenu() throws {
        let view = createViewWithEmptyMenu()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.PROFILE_SECTION)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.MENU_LIST)
        }
        try body.find(viewWithAccessibilityIdentifier: tags.JOIN_TEAM_BANNER)
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.PROFILE_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.MENU_LIST)
        try body.find(viewWithAccessibilityIdentifier: tags.JOIN_TEAM_BANNER)
    }

    // MARK: - Event Verification

    func simulateProfileTap() {
        let state = stateRobot.defaultState()
        state.eventSink(MoreEvent.ProfileClicked())
    }

    func simulateMenuItemTap(id: String) {
        let state = stateRobot.defaultState()
        state.eventSink(MoreEvent.MenuItemClicked(id: id))
    }

    func assertLastEvent(_ expected: MoreEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
