import ComposeApp

final class NutritionStateRobot: BaseStateRobot<NutritionUiState, NutritionEvent> {

    override func defaultState() -> NutritionUiState {
        NutritionUiState(
            url: "https://example.test/nutrition",
            eventSink: createEventSink()
        )
    }

    func loadingState() -> NutritionUiState {
        NutritionUiState(
            url: nil,
            eventSink: createEventSink()
        )
    }
}
