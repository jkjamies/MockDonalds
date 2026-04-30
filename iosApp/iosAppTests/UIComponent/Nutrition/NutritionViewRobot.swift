import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class NutritionViewRobot {

    private let stateRobot = NutritionStateRobot()
    private let tags = NutritionTestTags.shared

    // MARK: - State + View Creation

    func createDefaultView() -> NutritionView {
        NutritionView(state: stateRobot.defaultState())
    }

    func createLoadingView() -> NutritionView {
        NutritionView(state: stateRobot.loadingState())
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.SCREEN)
        try body.find(viewWithAccessibilityIdentifier: tags.WEBVIEW)
    }

    func assertLoadingScreen() throws {
        let view = createLoadingView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.SCREEN)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.WEBVIEW)
        }
    }

    // MARK: - Event Verification

    func simulateBackTap() {
        let state = stateRobot.defaultState()
        state.eventSink(NutritionEvent.BackClicked())
    }

    func assertLastEvent(_ expected: NutritionEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
