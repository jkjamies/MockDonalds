import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class RewardsViewRobot {

    private let stateRobot = RewardsStateRobot()
    private let tags = RewardsTestTags.shared

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

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.POINTS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.VAULT_SPECIALS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: "\(tags.FEATURED_VAULT_CARD)-1")
        try body.find(viewWithAccessibilityIdentifier: tags.HISTORY_SECTION)
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.POINTS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.VAULT_SPECIALS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.HISTORY_SECTION)
    }

    func assertScreenWithNoProgress() throws {
        let view = createViewWithNoProgress()
        let body = try view.inspect()
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.POINTS_SECTION)
        }
        try body.find(viewWithAccessibilityIdentifier: tags.VAULT_SPECIALS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.HISTORY_SECTION)
    }

    func assertScreenWithEmptyVault() throws {
        let view = createViewWithEmptyVault()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.POINTS_SECTION)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.VAULT_SPECIALS_SECTION)
        }
        try body.find(viewWithAccessibilityIdentifier: tags.HISTORY_SECTION)
    }

    func assertScreenWithEmptyHistory() throws {
        let view = createViewWithEmptyHistory()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.POINTS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.VAULT_SPECIALS_SECTION)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.HISTORY_SECTION)
        }
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
