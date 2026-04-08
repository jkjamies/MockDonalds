import SwiftUI
import Testing
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
        #expect(view.body != nil)
    }

    func assertViewWithNoProgressCreated() {
        let view = createViewWithNoProgress()
        #expect(view.body != nil)
    }

    func assertViewWithEmptyVaultCreated() {
        let view = createViewWithEmptyVault()
        #expect(view.body != nil)
    }

    func assertViewWithEmptyHistoryCreated() {
        let view = createViewWithEmptyHistory()
        #expect(view.body != nil)
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
        #expect(stateRobot.lastEvent == expected)
    }
}
