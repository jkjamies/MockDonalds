import ComposeApp

final class RecentsStateRobot: BaseStateRobot<RecentsUiState, RecentsEvent> {

    override func defaultState() -> RecentsUiState {
        RecentsUiState.Success(
            items: [
                RecentItem(
                    id: "1",
                    name: "Signature Stack Combo",
                    description: "Combo Meal",
                    relativeTime: "2 days ago",
                    imageUrl: nil
                ),
                RecentItem(
                    id: "2",
                    name: "Cookie Swirl Cup",
                    description: "Dessert",
                    relativeTime: "Last week",
                    imageUrl: nil
                ),
            ],
            eventSink: createEventSink()
        )
    }

    func loadingState() -> RecentsUiState {
        RecentsUiState.Loading(eventSink: createEventSink())
    }

    func emptyState() -> RecentsUiState {
        RecentsUiState.Empty(eventSink: createEventSink())
    }
}
