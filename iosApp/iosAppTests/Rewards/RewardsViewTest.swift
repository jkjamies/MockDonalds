import Testing
import ComposeApp
@testable import iosApp

@Suite struct RewardsViewTest {

    private let robot = RewardsViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    @Test func rendersWithNoProgress() {
        robot.assertViewWithNoProgressCreated()
    }

    @Test func rendersWithEmptyVault() {
        robot.assertViewWithEmptyVaultCreated()
    }

    @Test func rendersWithEmptyHistory() {
        robot.assertViewWithEmptyHistoryCreated()
    }

    // MARK: - Events

    @Test func viewAllTapEmitsEvent() {
        robot.simulateViewAllTap()
        robot.assertLastEvent(RewardsEvent.ViewAllClicked())
    }

    @Test func featuredVaultCardTapEmitsEvent() {
        robot.simulateVaultSpecialTap(id: "1")
        robot.assertLastEvent(RewardsEvent.VaultSpecialClicked(id: "1"))
    }

    @Test func vaultSpecialCardTapEmitsEvent() {
        robot.simulateVaultSpecialTap(id: "2")
        robot.assertLastEvent(RewardsEvent.VaultSpecialClicked(id: "2"))
    }
}
