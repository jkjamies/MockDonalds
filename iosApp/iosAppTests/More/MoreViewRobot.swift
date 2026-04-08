import SwiftUI
import XCTest
import ComposeApp
@testable import iosApp

final class MoreViewRobot {

    private let stateRobot = MoreStateRobot()

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

    // MARK: - Screen Assertions

    func assertDefaultViewCreated() {
        let view = createDefaultView()
        XCTAssertNotNil(view.body)
    }

    func assertViewWithNoProfileCreated() {
        let view = createViewWithNoProfile()
        XCTAssertNotNil(view.body)
    }

    func assertViewWithEmptyMenuCreated() {
        let view = createViewWithEmptyMenu()
        XCTAssertNotNil(view.body)
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
        XCTAssertEqual(stateRobot.lastEvent, expected)
    }
}
