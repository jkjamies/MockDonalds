import ComposeApp

final class LoginStateRobot: BaseStateRobot<LoginUiState, LoginEvent> {

    override func defaultState() -> LoginUiState {
        LoginUiState(
            logoUrl: "",
            email: "",
            isLoading: false,
            errorMessage: nil,
            eventSink: createEventSink()
        )
    }

    func stateWithEmail(_ email: String) -> LoginUiState {
        LoginUiState(
            logoUrl: "",
            email: email,
            isLoading: false,
            errorMessage: nil,
            eventSink: createEventSink()
        )
    }
}
