import SwiftUI
import XCTest
import ComposeApp
@testable import iosApp

final class ScanViewRobot {

    private let stateRobot = ScanStateRobot()

    // MARK: - State + View Creation

    func createDefaultView() -> ScanView {
        ScanView(state: stateRobot.defaultState())
    }

    func createViewWithNoMember() -> ScanView {
        ScanView(state: stateRobot.stateWithNoMember())
    }

    func createViewWithNoProgress() -> ScanView {
        ScanView(state: stateRobot.stateWithNoProgress())
    }

    // MARK: - Screen Assertions

    func assertDefaultViewCreated() {
        let view = createDefaultView()
        XCTAssertNotNil(view.body)
    }

    func assertViewWithNoMemberCreated() {
        let view = createViewWithNoMember()
        XCTAssertNotNil(view.body)
    }

    func assertViewWithNoProgressCreated() {
        let view = createViewWithNoProgress()
        XCTAssertNotNil(view.body)
    }

    // MARK: - Event Verification

    func simulatePayNowTap() {
        let state = stateRobot.defaultState()
        state.eventSink(ScanEvent.PayNowClicked())
    }

    func simulateViewOffersTap() {
        let state = stateRobot.defaultState()
        state.eventSink(ScanEvent.ViewOffersClicked())
    }

    func assertLastEvent(_ expected: ScanEvent) {
        XCTAssertEqual(stateRobot.lastEvent, expected)
    }
}
