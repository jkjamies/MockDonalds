import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class OrderViewRobot {

    private let stateRobot = OrderStateRobot()
    private let tags = OrderTestTags.shared

    // MARK: - State + View Creation

    func createDefaultView() -> OrderView {
        OrderView(state: stateRobot.defaultState())
    }

    func createViewWithNoCart() -> OrderView {
        OrderView(state: stateRobot.stateWithNoCart())
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: "\(tags.CATEGORY_PREVIEW_CARD)-burgers")
        try body.find(viewWithAccessibilityIdentifier: "\(tags.CATEGORY_PREVIEW_CARD)-drinks")
        try body.find(viewWithAccessibilityIdentifier: tags.CART_BAR)
    }

    func assertScreenWithNoCart() throws {
        let view = createViewWithNoCart()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: "\(tags.CATEGORY_PREVIEW_CARD)-burgers")
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.CART_BAR)
        }
    }

    // MARK: - Event Verification

    func simulateCategoryTap(id: String) {
        let state = stateRobot.defaultState()
        state.eventSink(OrderEvent.CategoryTapped(id: id))
    }

    func simulateCartTap() {
        let state = stateRobot.defaultState()
        state.eventSink(OrderEvent.CartClicked())
    }

    func assertLastEvent(_ expected: OrderEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
