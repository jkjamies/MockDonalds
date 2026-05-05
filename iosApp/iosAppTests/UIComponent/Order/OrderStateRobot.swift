import ComposeApp

final class OrderStateRobot: BaseStateRobot<OrderUiState, OrderEvent> {

    override func defaultState() -> OrderUiState {
        OrderUiState(
            categories: [
                MenuCategory(id: "1", name: "Burgers", iconUrl: nil),
                MenuCategory(id: "2", name: "Sides", iconUrl: nil),
            ],
            selectedCategoryId: "1",
            featuredItems: [
                FeaturedItem(
                    id: "1",
                    title: "Big Mac",
                    price: "$5.99",
                    description: "Classic burger",
                    imageUrl: "",
                    tag: "POPULAR",
                    isPrimary: true
                ),
            ],
            cartSummary: CartSummary(
                itemCount: 2,
                total: "$11.98"
            ),
            eventSink: createEventSink()
        )
    }

    func stateWithNoCart() -> OrderUiState {
        OrderUiState(
            categories: [
                MenuCategory(id: "1", name: "Burgers", iconUrl: nil),
            ],
            selectedCategoryId: "1",
            featuredItems: [
                FeaturedItem(
                    id: "1",
                    title: "Big Mac",
                    price: "$5.99",
                    description: "Classic burger",
                    imageUrl: "",
                    tag: "POPULAR",
                    isPrimary: true
                ),
            ],
            cartSummary: nil,
            eventSink: createEventSink()
        )
    }
}
