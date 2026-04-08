import ComposeApp

final class MoreStateRobot: BaseStateRobot<MoreUiState, MoreEvent> {

    override func defaultState() -> MoreUiState {
        MoreUiState(
            userProfile: UserProfile(name: "Test User", tier: "Gold", points: "2,450 pts", avatarUrl: ""),
            menuItems: [
                MoreMenuItem(id: "1", icon: "⚙️", title: "Settings"),
                MoreMenuItem(id: "2", icon: "❓", title: "Help"),
            ],
            eventSink: createEventSink()
        )
    }

    func stateWithNoProfile() -> MoreUiState {
        MoreUiState(
            userProfile: nil,
            menuItems: [
                MoreMenuItem(id: "1", icon: "⚙️", title: "Settings"),
                MoreMenuItem(id: "2", icon: "❓", title: "Help"),
            ],
            eventSink: createEventSink()
        )
    }

    func stateWithEmptyMenu() -> MoreUiState {
        MoreUiState(
            userProfile: UserProfile(name: "Test User", tier: "Gold", points: "2,450 pts", avatarUrl: ""),
            menuItems: [],
            eventSink: createEventSink()
        )
    }
}
