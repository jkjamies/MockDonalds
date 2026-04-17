import ComposeApp

final class RecentsStateRobot: BaseStateRobot<RecentsUiState, RecentsEvent> {

    override func defaultState() -> RecentsUiState {
        RecentsUiStateSuccess(
            items: [
                RecentItem(id: "1", name: "Big Mac Combo", description: "Combo Meal", relativeTime: "2 days ago", imageUrl: nil),
                RecentItem(id: "2", name: "McFlurry Oreo", description: "Dessert", relativeTime: "Last week", imageUrl: nil),
            ],
            eventSink: createEventSink()
        )
    }

    func loadingState() -> RecentsUiState {
        RecentsUiStateLoading(eventSink: createEventSink())
    }

    func emptyState() -> RecentsUiState {
        RecentsUiStateEmpty(eventSink: createEventSink())
    }
}
