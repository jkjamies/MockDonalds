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

    @Test func authRedirectPushesLoginScreen() {
        let manager = NavigationStateManager()

        // AuthInterceptor redirects to LoginScreen when unauthenticated
        manager.handle(actions: [NavigationAction.GoTo(screen: LoginScreen(returnTo: nil))])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is LoginScreen)
    }

    @Test func successfulAuthPopsThenNavigates() {
        let manager = NavigationStateManager()
        // Auth redirect pushed login
        manager.handle(actions: [NavigationAction.GoTo(screen: LoginScreen(returnTo: nil))])

        // After successful login: pop login, navigate to original destination
        manager.handle(actions: [
            NavigationAction.Pop(),
            NavigationAction.GoTo(screen: ProfileScreen.shared),
        ])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is ProfileScreen)
    }

    @Test func authRedirectFromDeepLinkFlow() {
        let manager = NavigationStateManager(initialTab: "home")

        // Deep link to profile (protected) while unauthenticated:
        // 1. Switch to more tab
        // 2. Auth interceptor redirects to login instead of profile
        manager.handle(actions: [
            NavigationAction.SwitchTab(tag: MoreScreen.shared.tag),
            NavigationAction.GoTo(screen: LoginScreen(returnTo: nil)),
        ])

        #expect(manager.selectedTab == MoreScreen.shared.tag)
        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is LoginScreen)
    }

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
