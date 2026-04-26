#if DEBUG
import ComposeApp

final class DebugMenuStateRobot: BaseStateRobot<DebugMenuUiState, DebugMenuEvent> {

    override func defaultState() -> DebugMenuUiState {
        DebugMenuUiState(
            entries: [
                DebugMenuEntry(
                    id: "feature-flags",
                    title: "Feature Flags",
                    subtitle: "Inspect and override remote flags",
                    target: FeatureFlagsDebugScreen.shared
                ),
                DebugMenuEntry(
                    id: "build-config",
                    title: "Build Config",
                    subtitle: "Current environment, SDK versions, and build metadata",
                    target: BuildConfigDebugScreen.shared
                ),
            ],
            eventSink: createEventSink()
        )
    }

    func stateWithNoEntries() -> DebugMenuUiState {
        DebugMenuUiState(entries: [], eventSink: createEventSink())
    }
}
#endif
