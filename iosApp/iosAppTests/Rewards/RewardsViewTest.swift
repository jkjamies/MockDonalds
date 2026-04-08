import XCTest
import ComposeApp
@testable import iosApp

final class RewardsViewTest: XCTestCase {

    private lazy var robot = RewardsViewRobot()

    // MARK: - Rendering

    func testRendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    func testRendersWithNoProgress() {
        robot.assertViewWithNoProgressCreated()
    }

    func testRendersWithEmptyVault() {
        robot.assertViewWithEmptyVaultCreated()
    }

    func testRendersWithEmptyHistory() {
        robot.assertViewWithEmptyHistoryCreated()
    }

    // MARK: - Events

    func testViewAllTapEmitsEvent() {
        robot.simulateViewAllTap()
        robot.assertLastEvent(RewardsEvent.ViewAllClicked())
    }

    func testFeaturedVaultCardTapEmitsEvent() {
        robot.simulateVaultSpecialTap(id: "1")
        robot.assertLastEvent(RewardsEvent.VaultSpecialClicked(id: "1"))
    }

    func testVaultSpecialCardTapEmitsEvent() {
        robot.simulateVaultSpecialTap(id: "2")
        robot.assertLastEvent(RewardsEvent.VaultSpecialClicked(id: "2"))
    }
}
