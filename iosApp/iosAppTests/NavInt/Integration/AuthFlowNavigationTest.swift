import Testing
import ComposeApp
@testable import iosApp

/// Tests auth interception behavior as it manifests in iOS navigation state.
/// Auth logic lives in Kotlin (InterceptingNavigator + AuthInterceptor), but the resulting
/// NavigationActions must be correctly processed by the iOS NavigationStateManager.
///
/// These tests verify the navigation state transitions that occur during auth flows,
/// complementing the Android navint-tests which test the same flows with real presenters.
@Suite @MainActor struct AuthFlowNavigationTest {

    // MARK: - Flow Presentation

    @Test func authRedirectPresentsFlow() {
        let manager = NavigationStateManager()

        // AuthInterceptor redirects to LoginScreen (FlowScreen) — BridgeNavigator emits PresentFlow
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        #expect(manager.isFlowActive)
        #expect(manager.flowRootScreen is LoginScreen)
        #expect(manager.navigationPath.isEmpty)
    }

    @Test func successfulAuthDismissesFlowAndNavigates() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        // After successful login: pop dismisses flow, goTo routes to main path
        manager.handle(actions: [
            NavigationAction.Pop(),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        #expect(!manager.isFlowActive)
        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    @Test func multiScreenAuthFlowNavigatesWithinFlow() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])

        // Inner auth screens push onto flow path
        manager.handle(actions: [NavigationAction.GoTo(screen: OrderScreen.shared)])

        #expect(manager.isFlowActive)
        #expect(manager.flowPath.count == 1)
        #expect(manager.navigationPath.isEmpty)
    }

    @Test func authFlowBackWithinFlowPopsFlowPath() {
        let manager = NavigationStateManager()
        manager.handle(actions: [NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil))])
        manager.handle(actions: [NavigationAction.GoTo(screen: OrderScreen.shared)])

        manager.handle(actions: [NavigationAction.Pop()])

        #expect(manager.isFlowActive)
        #expect(manager.flowPath.isEmpty)
    }

    @Test func authRedirectFromDeepLinkFlow() {
        let manager = NavigationStateManager(initialTab: "home")

        // Deep link to profile (protected) while unauthenticated:
        // 1. Switch to more tab
        // 2. Auth interceptor redirects to login (FlowScreen) — BridgeNavigator emits PresentFlow
        manager.handle(actions: [
            NavigationAction.SwitchTab(tag: MoreScreen.shared.tag),
            NavigationAction.PresentFlow(screen: LoginScreen(returnTo: nil)),
        ])

        #expect(manager.selectedTab == MoreScreen.shared.tag)
        #expect(manager.isFlowActive)
        #expect(manager.flowRootScreen is LoginScreen)
        #expect(manager.navigationPath.isEmpty)
    }

    // MARK: - Non-Flow (Authenticated)

    @Test func navigateToProtectedScreenWhenAuthenticated() {
        let manager = NavigationStateManager(initialTab: "more")

        // When authenticated, no interception — direct navigation to profile
        manager.handle(actions: [NavigationAction.GoTo(screen: ProfileScreen.shared)])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    @Test func logoutResetsToRoot() {
        let manager = NavigationStateManager(initialTab: "more")
        manager.handle(actions: [NavigationAction.GoTo(screen: ProfileScreen.shared)])

        // Logout resets navigation
        manager.handle(actions: [NavigationAction.ResetRoot(screen: HomeScreen.shared)])

        #expect(manager.navigationPath.isEmpty)
    }
}
