#if DEBUG
import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct FeatureFlagsDebugViewTest {

    private let robot = FeatureFlagsDebugViewRobot()

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    @Test func backTapEmitsEvent() {
        robot.simulateBackTap()
        robot.assertLastEvent(FeatureFlagsDebugEvent.BackClicked())
    }
}
#endif
