import ComposeApp

final class LoginStateRobot: BaseStateRobot<LoginUiState, LoginEvent> {

    override func defaultState() -> LoginUiState {
        LoginUiState(
            logoUrl: "",
            email: "",
            password: "",
            isLoading: false,
            errorMessage: nil,
            eventSink: createEventSink()
        )
    }

    func stateWithEmail(_ email: String) -> LoginUiState {
        LoginUiState(
            logoUrl: "",
            email: email,
            password: "",
            isLoading: false,
            errorMessage: nil,
            eventSink: createEventSink()
        )
    }

    func stateWithCredentials(
        email: String,
        password: String
    ) -> LoginUiState {
        LoginUiState(
            logoUrl: "",
            email: email,
            password: password,
            isLoading: false,
            errorMessage: nil,
            eventSink: createEventSink()
        )
    }
}
