#if DEBUG
import ComposeApp

final class BuildConfigDebugStateRobot: BaseStateRobot<BuildConfigDebugUiState, BuildConfigDebugEvent> {

    override func defaultState() -> BuildConfigDebugUiState {
        BuildConfigDebugUiState(
            fields: [
                BuildConfigField(name: "appId", value: "us-sampleplatter-mobile-int", group: .identity),
                BuildConfigField(name: "market", value: "us", group: .identity),
                BuildConfigField(name: "baseUrl", value: "https://int-api.sampleplatter.com", group: .urls),
                BuildConfigField(name: "locale", value: "en-US", group: .localization),
            ],
            eventSink: createEventSink(),
        )
    }
}
#endif
