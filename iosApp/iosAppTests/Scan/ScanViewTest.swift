import Testing
import ComposeApp
@testable import iosApp

@Suite struct ScanViewTest {

    private let robot = ScanViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    @Test func rendersWithNoMember() {
        robot.assertViewWithNoMemberCreated()
    }

    @Test func rendersWithNoProgress() {
        robot.assertViewWithNoProgressCreated()
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
