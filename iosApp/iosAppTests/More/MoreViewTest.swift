import XCTest
import ComposeApp
@testable import iosApp

final class MoreViewTest: XCTestCase {

    private lazy var robot = MoreViewRobot()

    // MARK: - Rendering

    func testRendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    func testRendersWithNoProfile() {
        robot.assertViewWithNoProfileCreated()
    }

    func testRendersWithEmptyMenu() {
        robot.assertViewWithEmptyMenuCreated()
    }

    // MARK: - Events

    func testProfileTapEmitsEvent() {
        robot.simulateProfileTap()
        robot.assertLastEvent(MoreEvent.ProfileClicked())
    }

    func testMenuItemTapEmitsEvent() {
        robot.simulateMenuItemTap(id: "1")
        robot.assertLastEvent(MoreEvent.MenuItemClicked(id: "1"))
    }
}
