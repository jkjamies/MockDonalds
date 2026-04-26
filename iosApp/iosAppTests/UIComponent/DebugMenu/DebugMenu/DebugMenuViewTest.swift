#if DEBUG
import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct DebugMenuViewTest {

    private let robot = DebugMenuViewRobot()

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersWithNoEntries() throws {
        try robot.assertScreenWithNoEntries()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    @Test func entryTapEmitsEvent() {
        robot.simulateEntryTap(id: "feature-flags")
        robot.assertLastEvent(DebugMenuEvent.EntryClicked(id: "feature-flags"))
    }

    @Test func backTapEmitsEvent() {
        robot.simulateBackTap()
        robot.assertLastEvent(DebugMenuEvent.BackClicked())
    }
}
#endif
