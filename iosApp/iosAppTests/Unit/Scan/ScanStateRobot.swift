import ComposeApp

final class ScanStateRobot: BaseStateRobot<ScanUiState, ScanEvent> {

    override func defaultState() -> ScanUiState {
        ScanUiState(
            memberInfo: MemberInfo(
                memberStatus: "Gold Member",
                qrCodeUrl: ""
            ),
            rewardsProgress: ScanRewardsProgress(
                currentPoints: 2450,
                pointsToNextReward: 550,
                progressFraction: 0.82,
                message: "550 points to your next reward"
            ),
            eventSink: createEventSink()
        )
    }

    func stateWithNoMember() -> ScanUiState {
        ScanUiState(
            memberInfo: nil,
            rewardsProgress: ScanRewardsProgress(
                currentPoints: 2450,
                pointsToNextReward: 550,
                progressFraction: 0.82,
                message: "550 points to your next reward"
            ),
            eventSink: createEventSink()
        )
    }

    func stateWithNoProgress() -> ScanUiState {
        ScanUiState(
            memberInfo: MemberInfo(
                memberStatus: "Gold Member",
                qrCodeUrl: ""
            ),
            rewardsProgress: nil,
            eventSink: createEventSink()
        )
    }
}
