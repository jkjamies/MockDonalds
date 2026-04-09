import Testing
import ComposeApp
@testable import iosApp

/// Tests tab switching behavior through NavigationStateManager.
/// Verifies iOS-native TabView state management works correctly with Circuit navigation actions.
@Suite @MainActor struct TabSwitchingTest {

    @Test func initialTabIsHome() {
        let manager = NavigationStateManager()

        #expect(manager.selectedTab == "home")
    }

    @Test func switchToEachTab() {
        let manager = NavigationStateManager()
        let tabs = [
            ("order", OrderScreen.shared.tag),
            ("rewards", RewardsScreen.shared.tag),
            ("scan", ScanScreen.shared.tag),
            ("more", MoreScreen.shared.tag),
            ("home", HomeScreen.shared.tag),
        ]

        for (_, tag) in tabs {
            manager.handle(actions: [NavigationAction.SwitchTab(tag: tag)])
            #expect(manager.selectedTab == tag)
        }
    }

    @Test func switchTabClearsNestedNavigation() {
        let manager = NavigationStateManager()

        // Navigate deep within home tab
        manager.handle(actions: [
            NavigationAction.GoTo(screen: OrderScreen.shared),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])
        #expect(manager.navigationPath.count == 2)

        // Switch tab — should clear the stack
        manager.handle(actions: [NavigationAction.SwitchTab(tag: "more")])

        #expect(manager.navigationPath.isEmpty)
        #expect(manager.selectedTab == "more")
    }

    @Test func switchBackToSameTabClearsPath() {
        let manager = NavigationStateManager(initialTab: "home")
        manager.handle(actions: [NavigationAction.GoTo(screen: ProfileScreen.shared)])

        // Tap same tab again — common UX pattern to "go home" within a tab
        manager.handle(actions: [NavigationAction.SwitchTab(tag: "home")])

        #expect(manager.navigationPath.isEmpty)
        #expect(manager.selectedTab == "home")
    }
}
