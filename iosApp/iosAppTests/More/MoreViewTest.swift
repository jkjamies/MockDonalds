import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct MoreViewTest {

    private let robot = MoreViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersWithNoProfile() throws {
        try robot.assertScreenWithNoProfile()
    }

    @Test func rendersWithEmptyMenu() throws {
        try robot.assertScreenWithEmptyMenu()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    // MARK: - Events

    @Test func profileTapEmitsEvent() {
        robot.simulateProfileTap()
        robot.assertLastEvent(MoreEvent.ProfileClicked())
    }

    @Test func menuItemTapEmitsEvent() {
        robot.simulateMenuItemTap(id: "1")
        robot.assertLastEvent(MoreEvent.MenuItemClicked(id: "1"))
    }
}
