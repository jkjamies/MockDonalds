import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class RecentsViewRobot {

    private let stateRobot = RecentsStateRobot()
    private let tags = RecentsTestTags.shared

    // MARK: - State + View Creation

    func createDefaultView() -> RecentsView {
        RecentsView(state: stateRobot.defaultState())
    }

    func createLoadingView() -> RecentsView {
        RecentsView(state: stateRobot.loadingState())
    }

    func createEmptyView() -> RecentsView {
        RecentsView(state: stateRobot.emptyState())
    }

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.SCREEN)
        try body.find(viewWithAccessibilityIdentifier: tags.LIST)
        try body.find(viewWithAccessibilityIdentifier: "\(tags.ITEM)-1")
        try body.find(viewWithAccessibilityIdentifier: "\(tags.ITEM)-2")
    }

    func assertLoadingScreen() throws {
        let view = createLoadingView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.SCREEN)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.LIST)
        }
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.EMPTY)
        }
    }

    func assertEmptyScreen() throws {
        let view = createEmptyView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.SCREEN)
        try body.find(viewWithAccessibilityIdentifier: tags.EMPTY)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.LIST)
        }
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.SCREEN)
        try body.find(viewWithAccessibilityIdentifier: tags.LIST)
    }

    // MARK: - Event Verification

    func simulateBackTap() {
        let state = stateRobot.defaultState()
        if let success = state as? RecentsUiState.Success {
            success.eventSink(RecentsEvent.OnBackTapped())
        }
    }

    func simulateItemTap(id: String) {
        let state = stateRobot.defaultState()
        if let success = state as? RecentsUiState.Success {
            success.eventSink(RecentsEvent.OnItemTapped(id: id))
        }
    }

    func assertLastEvent(_ expected: RecentsEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
