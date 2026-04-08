import ComposeApp

final class RewardsStateRobot: BaseStateRobot<RewardsUiState, RewardsEvent> {

    override func defaultState() -> RewardsUiState {
        RewardsUiState(
            progress: RewardsProgress(currentPoints: 2450, nextRewardName: "Free Fries", pointsToNextReward: 550, progressFraction: 0.82),
            vaultSpecials: [
                VaultSpecial(id: "1", title: "Double Quarter", pointsCost: "3000 PTS", imageUrl: "", tag: "LIMITED", isFeatured: true),
                VaultSpecial(id: "2", title: "McFlurry", pointsCost: "1500 PTS", imageUrl: "", tag: nil, isFeatured: false),
            ],
            history: [
                HistoryEntry(id: "1", title: "Big Mac Meal", subtitle: "Yesterday", points: "+150", isPositive: true, icon: "🍔"),
            ],
            eventSink: createEventSink()
        )
    }

    func stateWithNoProgress() -> RewardsUiState {
        RewardsUiState(
            progress: nil,
            vaultSpecials: [
                VaultSpecial(id: "1", title: "Double Quarter", pointsCost: "3000 PTS", imageUrl: "", tag: "LIMITED", isFeatured: true),
            ],
            history: [
                HistoryEntry(id: "1", title: "Big Mac Meal", subtitle: "Yesterday", points: "+150", isPositive: true, icon: "🍔"),
            ],
            eventSink: createEventSink()
        )
    }

    func stateWithEmptyVault() -> RewardsUiState {
        RewardsUiState(
            progress: RewardsProgress(currentPoints: 2450, nextRewardName: "Free Fries", pointsToNextReward: 550, progressFraction: 0.82),
            vaultSpecials: [],
            history: [
                HistoryEntry(id: "1", title: "Big Mac Meal", subtitle: "Yesterday", points: "+150", isPositive: true, icon: "🍔"),
            ],
            eventSink: createEventSink()
        )
    }

    func stateWithEmptyHistory() -> RewardsUiState {
        RewardsUiState(
            progress: RewardsProgress(currentPoints: 2450, nextRewardName: "Free Fries", pointsToNextReward: 550, progressFraction: 0.82),
            vaultSpecials: [
                VaultSpecial(id: "1", title: "Double Quarter", pointsCost: "3000 PTS", imageUrl: "", tag: "LIMITED", isFeatured: true),
            ],
            history: [],
            eventSink: createEventSink()
        )
    }
}
