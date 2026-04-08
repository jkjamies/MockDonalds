import Testing
import ComposeApp
@testable import iosApp

@Suite struct MoreViewTest {

    private let robot = MoreViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    @Test func rendersWithNoProfile() {
        robot.assertViewWithNoProfileCreated()
    }

    @Test func rendersWithEmptyMenu() {
        robot.assertViewWithEmptyMenuCreated()
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
