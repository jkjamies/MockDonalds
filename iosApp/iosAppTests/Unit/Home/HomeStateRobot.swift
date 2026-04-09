import ComposeApp

final class HomeStateRobot: BaseStateRobot<HomeUiState, HomeEvent> {

    override func defaultState() -> HomeUiState {
        HomeUiState(
            userName: "TestUser",
            heroPromotion: HeroPromotion(
                title: "Test Promo",
                description: "Description",
                tag: "NEW",
                imageUrl: "",
                ctaText: "Order Now"
            ),
            recentCravings: [
                Craving(id: "1", title: "Big Mac", subtitle: "Classic", imageUrl: ""),
            ],
            exploreItems: [
                ExploreItem(id: "1", icon: "star", title: "Deals", subtitle: "Save more"),
                ExploreItem(id: "2", icon: "gift", title: "Gifts", subtitle: "Share joy"),
            ],
            eventSink: createEventSink()
        )
    }

    func stateWithNoPromotion() -> HomeUiState {
        HomeUiState(
            userName: "TestUser",
            heroPromotion: nil,
            recentCravings: [
                Craving(id: "1", title: "Big Mac", subtitle: "Classic", imageUrl: ""),
            ],
            exploreItems: [
                ExploreItem(id: "1", icon: "star", title: "Deals", subtitle: "Save more"),
                ExploreItem(id: "2", icon: "gift", title: "Gifts", subtitle: "Share joy"),
            ],
            eventSink: createEventSink()
        )
    }

    func stateWithEmptyCravings() -> HomeUiState {
        HomeUiState(
            userName: "TestUser",
            heroPromotion: HeroPromotion(
                title: "Test Promo",
                description: "Description",
                tag: "NEW",
                imageUrl: "",
                ctaText: "Order Now"
            ),
            recentCravings: [],
            exploreItems: [
                ExploreItem(id: "1", icon: "star", title: "Deals", subtitle: "Save more"),
                ExploreItem(id: "2", icon: "gift", title: "Gifts", subtitle: "Share joy"),
            ],
            eventSink: createEventSink()
        )
    }
}
