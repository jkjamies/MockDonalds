import SwiftUI
import Testing
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
        #expect(view.body != nil)
    }

    func assertViewWithNoMemberCreated() {
        let view = createViewWithNoMember()
        #expect(view.body != nil)
    }

    func assertViewWithNoProgressCreated() {
        let view = createViewWithNoProgress()
        #expect(view.body != nil)
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
        #expect(stateRobot.lastEvent == expected)
    }
}
