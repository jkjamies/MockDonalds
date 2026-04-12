import Testing
import ComposeApp
@testable import iosApp

/// Tests navigation action handling in NavigationStateManager.
/// Mirrors Android navint-tests: verifies push, pop, resetRoot, tab switching, and deep links
/// work correctly through the iOS-native navigation infrastructure.
@Suite @MainActor struct NavigationStateManagerTest {

    // MARK: - GoTo (Push)

    @Test func goToPushesScreenOntoPath() {
        let manager = NavigationStateManager()

        manager.handle(actions: [NavigationAction.GoTo(screen: OrderScreen.shared)])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is OrderScreen)
    }

    @Test func multipleGoToPushesInOrder() {
        let manager = NavigationStateManager()

        manager.handle(actions: [
            NavigationAction.GoTo(screen: OrderScreen.shared),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        #expect(manager.navigationPath.count == 2)
        #expect(manager.navigationPath[0].screen is OrderScreen)
        #expect(manager.navigationPath[1].screen is ProfileScreen)
    }

    // MARK: - Pop

    @Test func popRemovesLastScreen() {
        let manager = NavigationStateManager()
        manager.handle(actions: [
            NavigationAction.GoTo(screen: OrderScreen.shared),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        manager.handle(actions: [NavigationAction.Pop()])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is OrderScreen)
    }

    @Test func popOnEmptyPathIsNoOp() {
        let manager = NavigationStateManager()

        manager.handle(actions: [NavigationAction.Pop()])

        #expect(manager.navigationPath.isEmpty)
    }

    // MARK: - ResetRoot

    @Test func resetRootClearsPath() {
        let manager = NavigationStateManager()
        manager.handle(actions: [
            NavigationAction.GoTo(screen: OrderScreen.shared),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        manager.handle(actions: [NavigationAction.ResetRoot(screen: HomeScreen.shared)])

        #expect(manager.navigationPath.isEmpty)
    }

    // MARK: - SwitchTab

    @Test func switchTabChangesSelectedTab() {
        let manager = NavigationStateManager(initialTab: "home")

        manager.handle(actions: [NavigationAction.SwitchTab(tag: "order")])

        #expect(manager.selectedTab == "order")
    }

    @Test func switchTabClearsNavigationPath() {
        let manager = NavigationStateManager(initialTab: "home")
        manager.handle(actions: [NavigationAction.GoTo(screen: ProfileScreen.shared)])

        manager.handle(actions: [NavigationAction.SwitchTab(tag: "more")])

        #expect(manager.navigationPath.isEmpty)
        #expect(manager.selectedTab == "more")
    }

    // MARK: - DeepLink

    @Test func deepLinkSetsFullPath() {
        let manager = NavigationStateManager()

        manager.handle(actions: [
            NavigationAction.DeepLink(screens: [MoreScreen.shared, ProfileScreen.shared]),
        ])

        #expect(manager.navigationPath.count == 2)
        #expect(manager.navigationPath[0].screen is MoreScreen)
        #expect(manager.navigationPath[1].screen is ProfileScreen)
    }

    @Test func deepLinkReplacesExistingPath() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.GoTo(screen: OrderScreen.shared)])

        manager.handle(actions: [
            NavigationAction.DeepLink(screens: [ProfileScreen.shared]),
        ])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    // MARK: - Batched Actions

    @Test func batchedPopThenGoToProducesCorrectState() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.GoTo(screen: LoginScreen(returnTo: nil))])

        // Simulates presenter event: pop login, go to profile (after successful auth)
        manager.handle(actions: [
            NavigationAction.Pop(),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    @Test func batchedSwitchTabThenGoTo() {
        let manager = NavigationStateManager(initialTab: "home")

        // Deep link: switch to more tab, then push profile
        manager.handle(actions: [
            NavigationAction.SwitchTab(tag: "more"),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        #expect(manager.selectedTab == "more")
        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    // MARK: - Flow

    @Test func presentFlowSetsFlowRootScreen() {
        let manager = NavigationStateManager()

        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        #expect(manager.isFlowActive)
        #expect(manager.flowRootScreen is LoginScreen)
        #expect(manager.flowPath.isEmpty)
    }

    @Test func goToDuringFlowPushesOntoFlowPath() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        manager.handle(actions: [NavigationAction.GoTo(screen: ProfileScreen.shared)])

        #expect(manager.flowPath.count == 1)
        #expect(manager.flowPath[0].screen is ProfileScreen)
        #expect(manager.navigationPath.isEmpty)
    }

    @Test func popDuringFlowPopsFromFlowPath() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])
        manager.handle(actions: [
            NavigationAction.GoTo(screen: OrderScreen.shared),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        manager.handle(actions: [NavigationAction.Pop()])

        #expect(manager.flowPath.count == 1)
        #expect(manager.flowPath[0].screen is OrderScreen)
        #expect(manager.isFlowActive)
    }

    @Test func popOnEmptyFlowPathDismissesFlow() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        manager.handle(actions: [NavigationAction.Pop()])

        #expect(!manager.isFlowActive)
        #expect(manager.flowRootScreen == nil)
        #expect(manager.flowPath.isEmpty)
    }

    @Test func dismissFlowClearsFlowState() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])
        manager.handle(actions: [NavigationAction.GoTo(screen: ProfileScreen.shared)])

        manager.handle(actions: [NavigationAction.DismissFlow()])

        #expect(!manager.isFlowActive)
        #expect(manager.flowRootScreen == nil)
        #expect(manager.flowPath.isEmpty)
    }

    @Test func goToAfterFlowDismissedGoesToMainPath() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        // Auth success: pop dismisses flow, then goTo routes to main path
        manager.handle(actions: [
            NavigationAction.Pop(),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        #expect(!manager.isFlowActive)
        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    @Test func resetRootDismissesActiveFlow() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])
        manager.handle(actions: [NavigationAction.GoTo(screen: ProfileScreen.shared)])

        manager.handle(actions: [NavigationAction.ResetRoot(screen: HomeScreen.shared)])

        #expect(!manager.isFlowActive)
        #expect(manager.flowPath.isEmpty)
        #expect(manager.navigationPath.isEmpty)
    }

    @Test func switchTabDismissesActiveFlow() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        manager.handle(actions: [NavigationAction.SwitchTab(tag: "order")])

        #expect(!manager.isFlowActive)
        #expect(manager.selectedTab == "order")
    }

    @Test func deepLinkDismissesActiveFlow() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        manager.handle(actions: [
            NavigationAction.DeepLink(screens: [MoreScreen.shared, ProfileScreen.shared]),
        ])

        #expect(!manager.isFlowActive)
        #expect(manager.navigationPath.count == 2)
    }

    @Test func flowDoesNotAffectMainNavigationPath() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.GoTo(screen: OrderScreen.shared)])

        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])
        manager.handle(actions: [NavigationAction.GoTo(screen: ProfileScreen.shared)])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is OrderScreen)
        #expect(manager.flowPath.count == 1)
        #expect(manager.flowPath[0].screen is ProfileScreen)
    }

    @Test func presentFlowWhileFlowActiveReplacesFlow() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])
        manager.handle(actions: [NavigationAction.GoTo(screen: OrderScreen.shared)])

        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        #expect(manager.isFlowActive)
        #expect(manager.flowPath.isEmpty)
    }
}
