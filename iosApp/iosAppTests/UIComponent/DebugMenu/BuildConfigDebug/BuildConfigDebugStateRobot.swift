#if DEBUG
import ComposeApp

final class BuildConfigDebugStateRobot: BaseStateRobot<BuildConfigDebugUiState, BuildConfigDebugEvent> {

    override func defaultState() -> BuildConfigDebugUiState {
        BuildConfigDebugUiState(eventSink: createEventSink())
    }
}
#endif
