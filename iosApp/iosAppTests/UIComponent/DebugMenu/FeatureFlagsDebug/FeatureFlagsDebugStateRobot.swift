#if DEBUG
import ComposeApp

final class FeatureFlagsDebugStateRobot: BaseStateRobot<FeatureFlagsDebugUiState, FeatureFlagsDebugEvent> {

    override func defaultState() -> FeatureFlagsDebugUiState {
        FeatureFlagsDebugUiState(rows: [], eventSink: createEventSink())
    }
}
#endif
