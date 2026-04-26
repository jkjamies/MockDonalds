#if DEBUG
import Testing
import ComposeApp
@testable import iosApp

@Suite @MainActor struct DebugMenuNavigationTest {

    @Test func goToDebugMenuPushesScreen() {
        let manager = NavigationStateManager()

        manager.handle(actions: [NavigationAction.GoTo(screen: DebugMenuScreen.shared)])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is DebugMenuScreen)
    }

    @Test func debugMenuToFeatureFlagsBuildsNestedPath() {
        let manager = NavigationStateManager()

        manager.handle(actions: [
            NavigationAction.GoTo(screen: DebugMenuScreen.shared),
            NavigationAction.GoTo(screen: FeatureFlagsDebugScreen.shared),
        ])

        #expect(manager.navigationPath.count == 2)
        #expect(manager.navigationPath[0].screen is DebugMenuScreen)
        #expect(manager.navigationPath[1].screen is FeatureFlagsDebugScreen)
    }

    @Test func debugMenuToBuildConfigBuildsNestedPath() {
        let manager = NavigationStateManager()

        manager.handle(actions: [
            NavigationAction.GoTo(screen: DebugMenuScreen.shared),
            NavigationAction.GoTo(screen: BuildConfigDebugScreen.shared),
        ])

        #expect(manager.navigationPath.count == 2)
        #expect(manager.navigationPath[0].screen is DebugMenuScreen)
        #expect(manager.navigationPath[1].screen is BuildConfigDebugScreen)
    }

    @Test func popFromFeatureFlagsReturnsToDebugMenu() {
        let manager = NavigationStateManager()
        manager.handle(actions: [
            NavigationAction.GoTo(screen: DebugMenuScreen.shared),
            NavigationAction.GoTo(screen: FeatureFlagsDebugScreen.shared),
        ])

        manager.handle(actions: [NavigationAction.Pop()])

        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is DebugMenuScreen)
    }

    @Test func debugMenuOnMoreTab() {
        let manager = NavigationStateManager(initialTab: "home")

        manager.handle(actions: [
            NavigationAction.SwitchTab(tag: MoreScreen.shared.tag),
            NavigationAction.GoTo(screen: DebugMenuScreen.shared),
        ])

        #expect(manager.selectedTab == MoreScreen.shared.tag)
        #expect(manager.navigationPath.count == 1)
        #expect(manager.navigationPath[0].screen is DebugMenuScreen)
    }
}
#endif
