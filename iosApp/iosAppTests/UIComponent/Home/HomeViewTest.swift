import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct HomeViewTest {

    private let robot = HomeViewRobot()

    // MARK: - Rendering

    @Test func rendersDefaultState() throws {
        try robot.assertDefaultScreen()
    }

    @Test func rendersLandscapeLayout() throws {
        try robot.assertLandscapeScreen()
    }

    @Test func rendersWithNoPromotion() throws {
        try robot.assertScreenWithNoPromotion()
    }

    @Test func rendersWithEmptyCravings() throws {
        try robot.assertScreenWithEmptyCravings()
    }

    // MARK: - Events

    @Test func heroCtaTapEmitsEvent() {
        robot.simulateHeroCtaTap()
        robot.assertLastEvent(HomeEvent.HeroCtaClicked())
    }

    @Test func cravingTapEmitsEvent() {
        robot.simulateCravingTap(id: "1")
        robot.assertLastEvent(HomeEvent.CravingClicked(id: "1"))
    }

    @Test func exploreItemTapEmitsEvent() {
        robot.simulateExploreItemTap(id: "1")
        robot.assertLastEvent(HomeEvent.ExploreItemClicked(id: "1"))
    }
}
