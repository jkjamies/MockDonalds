import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct ScanViewTest {

    private let robot = ScanViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    @Test func rendersWithNoMember() throws {
        try robot.assertScreenWithNoMember()
    }

    @Test func rendersWithNoProgress() throws {
        try robot.assertScreenWithNoProgress()
    }

    // MARK: - Events

    @Test func payNowTapEmitsEvent() {
        robot.simulatePayNowTap()
        robot.assertLastEvent(ScanEvent.PayNowClicked())
    }

    @Test func viewOffersTapEmitsEvent() {
        robot.simulateViewOffersTap()
        robot.assertLastEvent(ScanEvent.ViewOffersClicked())
    }
}
