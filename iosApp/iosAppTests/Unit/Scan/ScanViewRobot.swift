import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class ScanViewRobot {

    private let stateRobot = ScanStateRobot()
    private let tags = ScanTestTags.shared

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

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.MEMBER_CARD)
        try body.find(viewWithAccessibilityIdentifier: tags.REWARDS_PROGRESS)
        try body.find(viewWithAccessibilityIdentifier: tags.PAY_NOW_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.VIEW_OFFERS_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.PRO_TIP)
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.MEMBER_CARD)
        try body.find(viewWithAccessibilityIdentifier: tags.PAY_NOW_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.VIEW_OFFERS_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.PRO_TIP)
    }

    func assertScreenWithNoMember() throws {
        let view = createViewWithNoMember()
        let body = try view.inspect()
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.MEMBER_CARD)
        }
        try body.find(viewWithAccessibilityIdentifier: tags.PAY_NOW_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.VIEW_OFFERS_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.PRO_TIP)
    }

    func assertScreenWithNoProgress() throws {
        let view = createViewWithNoProgress()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.MEMBER_CARD)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.REWARDS_PROGRESS)
        }
        try body.find(viewWithAccessibilityIdentifier: tags.PAY_NOW_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: tags.VIEW_OFFERS_BUTTON)
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
