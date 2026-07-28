import ComposeApp

final class CategoryDetailStateRobot: BaseStateRobot<CategoryDetailUiState, CategoryDetailEvent> {

    override func defaultState() -> CategoryDetailUiState {
        CategoryDetailUiState(
            categoryId: "burgers",
            categoryName: "Burgers",
            items: [
                MenuItem(
                    id: "1",
                    title: "Signature Stack",
                    restaurantChain: "Sample Platter",
                    imageUrl: "",
                    servingSize: "214g",
                    categoryId: "burgers"
                ),
            ],
            cartSummary: CartSummary(itemCount: 2, total: "$11.98"),
            eventSink: createEventSink()
        )
    }

    func stateWithNoCart() -> CategoryDetailUiState {
        CategoryDetailUiState(
            categoryId: "burgers",
            categoryName: "Burgers",
            items: [
                MenuItem(
                    id: "1",
                    title: "Signature Stack",
                    restaurantChain: "Sample Platter",
                    imageUrl: "",
                    servingSize: "214g",
                    categoryId: "burgers"
                ),
            ],
            cartSummary: nil,
            eventSink: createEventSink()
        )
    }

    func stateWithNoItems() -> CategoryDetailUiState {
        CategoryDetailUiState(
            categoryId: "burgers",
            categoryName: "Burgers",
            items: [],
            cartSummary: CartSummary(itemCount: 2, total: "$11.98"),
            eventSink: createEventSink()
        )
    }
}
