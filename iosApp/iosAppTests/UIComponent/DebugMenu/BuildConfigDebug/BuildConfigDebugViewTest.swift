#if DEBUG
import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct BuildConfigDebugViewTest {

    private let robot = BuildConfigDebugViewRobot()

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    @Test func backTapEmitsEvent() {
        robot.simulateBackTap()
        robot.assertLastEvent(BuildConfigDebugEvent.BackClicked())
    }
}
#endif
