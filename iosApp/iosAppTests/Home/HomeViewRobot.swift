import SwiftUI
import Testing
import ViewInspector
import ComposeApp
@testable import iosApp

@MainActor
final class HomeViewRobot {

    private let stateRobot = HomeStateRobot()
    private let tags = HomeTestTags.shared

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

    func createLandscapeView() -> some View {
        createDefaultView()
            .environment(\.verticalSizeClass, .compact)
    }

    // MARK: - Screen Assertions

    func assertDefaultScreen() throws {
        let view = createDefaultView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.USER_NAME)
        try body.find(viewWithAccessibilityIdentifier: tags.HERO_BANNER)
        try body.find(viewWithAccessibilityIdentifier: tags.RECENT_CRAVINGS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: "\(tags.CRAVING_CARD)-1")
        try body.find(viewWithAccessibilityIdentifier: tags.EXPLORE_SECTION)
        try body.find(viewWithAccessibilityIdentifier: "\(tags.EXPLORE_ITEM)-1")
        try body.find(viewWithAccessibilityIdentifier: "\(tags.EXPLORE_ITEM)-2")
    }

    func assertLandscapeScreen() throws {
        let view = createLandscapeView()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.USER_NAME)
        try body.find(viewWithAccessibilityIdentifier: tags.HERO_BANNER)
        try body.find(viewWithAccessibilityIdentifier: tags.RECENT_CRAVINGS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.EXPLORE_SECTION)
    }

    func assertScreenWithNoPromotion() throws {
        let view = createViewWithNoPromotion()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.USER_NAME)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.HERO_BANNER)
        }
        try body.find(viewWithAccessibilityIdentifier: tags.RECENT_CRAVINGS_SECTION)
        try body.find(viewWithAccessibilityIdentifier: tags.EXPLORE_SECTION)
    }

    func assertScreenWithEmptyCravings() throws {
        let view = createViewWithEmptyCravings()
        let body = try view.inspect()
        try body.find(viewWithAccessibilityIdentifier: tags.USER_NAME)
        try body.find(viewWithAccessibilityIdentifier: tags.HERO_BANNER)
        #expect(throws: Error.self) {
            try body.find(viewWithAccessibilityIdentifier: self.tags.RECENT_CRAVINGS_SECTION)
        }
        try body.find(viewWithAccessibilityIdentifier: tags.EXPLORE_SECTION)
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
