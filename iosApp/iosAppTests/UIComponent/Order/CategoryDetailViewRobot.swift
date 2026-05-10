import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class CategoryDetailViewRobot {

    private let stateRobot = CategoryDetailStateRobot()
    private let tags = CategoryDetailTestTags.shared

    func createDefaultView() -> CategoryDetailView {
        CategoryDetailView(state: stateRobot.defaultState())
    }

    func createViewWithNoItems() -> CategoryDetailView {
        CategoryDetailView(state: stateRobot.stateWithNoItems())
    }

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.TOP_BAR)
        try body.find(viewWithAccessibilityIdentifier: tags.BACK_BUTTON)
        try body.find(viewWithAccessibilityIdentifier: "\(tags.MENU_ITEM_CARD)-1")
        try body.find(viewWithAccessibilityIdentifier: tags.CART_BAR)
    }

    func assertEmptyScreen() throws {
        let view = createViewWithNoItems()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.TOP_BAR)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: "\(self.tags.MENU_ITEM_CARD)-1")
        }
    }

    func simulateBackTap() {
        let state = stateRobot.defaultState()
        state.eventSink(CategoryDetailEvent.BackPressed())
    }

    func simulateAddToOrder(itemId: String) {
        let state = stateRobot.defaultState()
        state.eventSink(CategoryDetailEvent.AddToOrder(itemId: itemId))
    }

    func simulateCartTap() {
        let state = stateRobot.defaultState()
        state.eventSink(CategoryDetailEvent.CartClicked())
    }

    func assertLastEvent(_ expected: CategoryDetailEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
