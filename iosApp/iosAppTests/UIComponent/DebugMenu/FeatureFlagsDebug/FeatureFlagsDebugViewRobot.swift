#if DEBUG
import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class FeatureFlagsDebugViewRobot {

    private let stateRobot = FeatureFlagsDebugStateRobot()
    private let tags = FeatureFlagsDebugTestTags.shared

    func createDefaultView() -> FeatureFlagsDebugView {
        FeatureFlagsDebugView(state: stateRobot.defaultState())
    }

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.ROOT)
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.ROOT)
    }

    func simulateBackTap() {
        let state = stateRobot.defaultState()
        state.eventSink(FeatureFlagsDebugEvent.BackClicked())
    }

    func assertLastEvent(_ expected: FeatureFlagsDebugEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
#endif
