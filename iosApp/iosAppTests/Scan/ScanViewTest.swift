import XCTest
import ComposeApp
@testable import iosApp

final class ScanViewTest: XCTestCase {

    private lazy var robot = ScanViewRobot()

    // MARK: - Rendering

    func testRendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    func testRendersWithNoMember() {
        robot.assertViewWithNoMemberCreated()
    }

    func testRendersWithNoProgress() {
        robot.assertViewWithNoProgressCreated()
    }

    // MARK: - Events

    func testPayNowTapEmitsEvent() {
        robot.simulatePayNowTap()
        robot.assertLastEvent(ScanEvent.PayNowClicked())
    }

    func testViewOffersTapEmitsEvent() {
        robot.simulateViewOffersTap()
        robot.assertLastEvent(ScanEvent.ViewOffersClicked())
    }
}
