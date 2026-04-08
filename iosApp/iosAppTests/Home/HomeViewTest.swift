import XCTest
import ComposeApp
@testable import iosApp

final class HomeViewTest: XCTestCase {

    private lazy var robot = HomeViewRobot()

    // MARK: - Rendering

    func testRendersDefaultState() {
        robot.assertDefaultViewCreated()
    }

    func testRendersWithNoPromotion() {
        robot.assertViewWithNoPromotionCreated()
    }

    func testRendersWithEmptyCravings() {
        robot.assertViewWithEmptyCravingsCreated()
    }

    // MARK: - Events

    func testHeroCtaTapEmitsEvent() {
        robot.simulateHeroCtaTap()
        robot.assertLastEvent(HomeEvent.HeroCtaClicked())
    }

    func testCravingTapEmitsEvent() {
        robot.simulateCravingTap(id: "1")
        robot.assertLastEvent(HomeEvent.CravingClicked(id: "1"))
    }

    func testExploreItemTapEmitsEvent() {
        robot.simulateExploreItemTap(id: "1")
        robot.assertLastEvent(HomeEvent.ExploreItemClicked(id: "1"))
    }
}
