import ComposeApp

final class ProfileStateRobot: BaseStateRobot<ProfileUiState, ProfileEvent> {

    override func defaultState() -> ProfileUiState {
        ProfileUiState(
            name: "Night Owl",
            email: "gourmet@night.com",
            tier: "Gold",
            points: "4,280 pts",
            avatarUrl: "",
            memberSince: "Member since 2024",
            eventSink: createEventSink()
        )
    }
}
