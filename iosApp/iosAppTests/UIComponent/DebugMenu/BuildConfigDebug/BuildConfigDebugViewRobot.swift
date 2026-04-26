#if DEBUG
import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class BuildConfigDebugViewRobot {

    private let stateRobot = BuildConfigDebugStateRobot()
    private let tags = BuildConfigDebugTestTags.shared

    func createDefaultView() -> BuildConfigDebugView {
        BuildConfigDebugView(state: stateRobot.defaultState())
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
        state.eventSink(BuildConfigDebugEvent.BackClicked())
    }

    func assertLastEvent(_ expected: BuildConfigDebugEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
#endif
