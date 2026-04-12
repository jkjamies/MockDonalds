import ComposeApp

final class WelcomeStateRobot: BaseStateRobot<WelcomeUiState, WelcomeEvent> {

    override func defaultState() -> WelcomeUiState {
        WelcomeUiState(
            eventSink: createEventSink()
        )
    }
}
