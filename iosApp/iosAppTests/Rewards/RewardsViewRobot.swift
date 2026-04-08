import SwiftUI
import XCTest
import ComposeApp
@testable import iosApp

final class RewardsViewRobot {

    private let stateRobot = RewardsStateRobot()

    // MARK: - State + View Creation

    func createDefaultView() -> RewardsView {
        RewardsView(state: stateRobot.defaultState())
    }

    func createViewWithNoProgress() -> RewardsView {
        RewardsView(state: stateRobot.stateWithNoProgress())
    }

    func createViewWithEmptyVault() -> RewardsView {
        RewardsView(state: stateRobot.stateWithEmptyVault())
    }

    func createViewWithEmptyHistory() -> RewardsView {
        RewardsView(state: stateRobot.stateWithEmptyHistory())
    }

    // MARK: - Screen Assertions

    func assertDefaultViewCreated() {
        let view = createDefaultView()
        XCTAssertNotNil(view.body)
    }

    func assertViewWithNoProgressCreated() {
        let view = createViewWithNoProgress()
        XCTAssertNotNil(view.body)
    }

    func assertViewWithEmptyVaultCreated() {
        let view = createViewWithEmptyVault()
        XCTAssertNotNil(view.body)
    }

    func assertViewWithEmptyHistoryCreated() {
        let view = createViewWithEmptyHistory()
        XCTAssertNotNil(view.body)
    }

    // MARK: - Event Verification

    func simulateViewAllTap() {
        let state = stateRobot.defaultState()
        state.eventSink(RewardsEvent.ViewAllClicked())
    }

    func simulateVaultSpecialTap(id: String) {
        let state = stateRobot.defaultState()
        state.eventSink(RewardsEvent.VaultSpecialClicked(id: id))
    }

    func assertLastEvent(_ expected: RewardsEvent) {
        XCTAssertEqual(stateRobot.lastEvent, expected)
    }
}
