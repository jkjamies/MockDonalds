import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct RewardsViewTest {

    private let robot = RewardsViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    @Test func rendersWithNoProgress() throws {
        try robot.assertScreenWithNoProgress()
    }

    @Test func rendersWithEmptyVault() throws {
        try robot.assertScreenWithEmptyVault()
    }

    @Test func rendersWithEmptyHistory() throws {
        try robot.assertScreenWithEmptyHistory()
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
