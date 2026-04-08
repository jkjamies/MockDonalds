import SwiftUI
import Testing
import ComposeApp
@testable import iosApp

final class OrderViewRobot {

    private let stateRobot = OrderStateRobot()

    // MARK: - State + View Creation

    func createDefaultView() -> OrderView {
        OrderView(state: stateRobot.defaultState())
    }

    func createViewWithNoCart() -> OrderView {
        OrderView(state: stateRobot.stateWithNoCart())
    }

    // MARK: - Screen Assertions

    func assertDefaultViewCreated() {
        let view = createDefaultView()
        #expect(view.body != nil)
    }

    func assertViewWithNoCartCreated() {
        let view = createViewWithNoCart()
        #expect(view.body != nil)
    }

    // MARK: - Event Verification

    func simulateCategoryTap(id: String) {
        let state = stateRobot.defaultState()
        state.eventSink(OrderEvent.CategorySelected(id: id))
    }

    func simulateAddToOrder(itemId: String) {
        let state = stateRobot.defaultState()
        state.eventSink(OrderEvent.AddToOrder(itemId: itemId))
    }

    func simulateCartTap() {
        let state = stateRobot.defaultState()
        state.eventSink(OrderEvent.CartClicked())
    }

    func assertLastEvent(_ expected: OrderEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
