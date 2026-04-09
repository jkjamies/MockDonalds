import Testing
import ComposeApp
@testable import iosApp

/// Tests deep link resolution through the iOS navigation stack.
/// Verifies that deep link NavigationActions correctly set up the navigation path,
/// including tab switching for tab-rooted deep links.
@Suite @MainActor struct DeepLinkNavigationTest {

    @Test func deepLinkToProfileViaMoreTab() {
        let manager = NavigationStateManager(initialTab: "home")

        // mockdonalds://app/more/profile → SwitchTab(more) + GoTo(profile)
        manager.handle(actions: [
            NavigationAction.SwitchTab(tag: MoreScreen.shared.tag),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        #expect(manager.selectedTab == MoreScreen.shared.tag)
        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    @Test func deepLinkReplacesExistingNavigation() {
        let manager = NavigationStateManager(initialTab: "home")
        manager.handle(actions: [NavigationAction.GoTo(screen: OrderScreen.shared)])

        // New deep link arrives — should replace current path
        manager.handle(actions: [
            NavigationAction.DeepLink(screens: [ProfileScreen.shared]),
        ])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    @Test func deepLinkWithMultipleScreensBuildsFullStack() {
        let manager = NavigationStateManager()

        // Deep link that builds a navigation stack
        manager.handle(actions: [
            NavigationAction.DeepLink(screens: [MoreScreen.shared, ProfileScreen.shared]),
        ])

        #expect(manager.navigationPath.count == 2)
        #expect(manager.navigationPath[0].screen is MoreScreen)
        #expect(manager.navigationPath[1].screen is ProfileScreen)
    }

    @Test func deepLinkToTabRootOnly() {
        let manager = NavigationStateManager(initialTab: "home")

        // Deep link to just a tab (e.g. mockdonalds://app/order)
        manager.handle(actions: [
            NavigationAction.SwitchTab(tag: OrderScreen.shared.tag),
        ])

        #expect(manager.selectedTab == OrderScreen.shared.tag)
        #expect(manager.navigationPath.isEmpty)
    }
}
