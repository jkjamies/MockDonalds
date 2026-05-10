import ComposeApp

final class OrderStateRobot: BaseStateRobot<OrderUiState, OrderEvent> {

    override func defaultState() -> OrderUiState {
        OrderUiState(
            categoryPreviews: [
                CategoryPreview(id: "burgers", name: "Burgers", firstItemImageUrl: nil, itemCount: 2),
                CategoryPreview(id: "drinks", name: "Drinks", firstItemImageUrl: nil, itemCount: 0),
            ],
            cartSummary: CartSummary(itemCount: 2, total: "$11.98"),
            eventSink: createEventSink()
        )
    }

    func stateWithNoCart() -> OrderUiState {
        OrderUiState(
            categoryPreviews: [
                CategoryPreview(id: "burgers", name: "Burgers", firstItemImageUrl: nil, itemCount: 2),
            ],
            cartSummary: nil,
            eventSink: createEventSink()
        )
    }
}
