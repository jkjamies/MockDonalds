import SwiftUI
import Testing
import ComposeApp
@testable import iosApp

final class HomeViewRobot {

    private let stateRobot = HomeStateRobot()

    // MARK: - State + View Creation

    func createDefaultView() -> HomeView {
        HomeView(state: stateRobot.defaultState())
    }

    func createViewWithNoPromotion() -> HomeView {
        HomeView(state: stateRobot.stateWithNoPromotion())
    }

    func createViewWithEmptyCravings() -> HomeView {
        HomeView(state: stateRobot.stateWithEmptyCravings())
    }

    // MARK: - Screen Assertions

    func assertDefaultViewCreated() {
        let view = createDefaultView()
        #expect(view.body != nil)
    }

    func assertViewWithNoPromotionCreated() {
        let view = createViewWithNoPromotion()
        #expect(view.body != nil)
    }

    func assertViewWithEmptyCravingsCreated() {
        let view = createViewWithEmptyCravings()
        #expect(view.body != nil)
    }

    // MARK: - Event Verification

    func simulateHeroCtaTap() {
        let state = stateRobot.defaultState()
        state.eventSink(HomeEvent.HeroCtaClicked())
    }

    func simulateCravingTap(id: String) {
        let state = stateRobot.defaultState()
        state.eventSink(HomeEvent.CravingClicked(id: id))
    }

    func simulateExploreItemTap(id: String) {
        let state = stateRobot.defaultState()
        state.eventSink(HomeEvent.ExploreItemClicked(id: id))
    }

    func assertLastEvent(_ expected: HomeEvent) {
        #expect(stateRobot.lastEvent == expected)
    }
}
