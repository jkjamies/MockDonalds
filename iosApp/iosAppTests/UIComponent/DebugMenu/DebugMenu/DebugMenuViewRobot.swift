#if DEBUG
import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class DebugMenuViewRobot {

    private let stateRobot = DebugMenuStateRobot()
    private let tags = DebugMenuTestTags.shared

    func createDefaultView() -> DebugMenuView {
        DebugMenuView(state: stateRobot.defaultState())
    }

    func createViewWithNoEntries() -> DebugMenuView {
        DebugMenuView(state: stateRobot.stateWithNoEntries())
    }

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.ROOT)
        try body.find(viewWithAccessibilityIdentifier: tags.ENTRY_LIST)
        try body.find(viewWithAccessibilityIdentifier: "\(tags.ENTRY_ITEM)-feature-flags")
        try body.find(viewWithAccessibilityIdentifier: "\(tags.ENTRY_ITEM)-build-config")
    }

    func assertScreenWithNoEntries() throws {
        let view = createViewWithNoEntries()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.ROOT)
        try body.find(viewWithAccessibilityIdentifier: tags.ENTRY_LIST)
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.ROOT)
        try body.find(viewWithAccessibilityIdentifier: tags.ENTRY_LIST)
    }

    func simulateEntryTap(id: String) {
        let state = stateRobot.defaultState()
        state.eventSink(DebugMenuEvent.EntryClicked(id: id))
    }

    func simulateBackTap() {
        let state = stateRobot.defaultState()
        state.eventSink(DebugMenuEvent.BackClicked())
    }

    func assertLastEvent(_ expected: DebugMenuEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
#endif
