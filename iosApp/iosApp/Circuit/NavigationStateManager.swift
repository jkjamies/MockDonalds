import SwiftUI
import ComposeApp

/// Manages navigation state independently of SwiftUI views, making navigation logic testable.
///
/// Extracted from `CircuitNavigator` so that action handling (push, pop, reset, tab switch, deep link,
/// flow presentation) can be unit tested without requiring a SwiftUI view hierarchy.
final class NavigationStateManager: ObservableObject {
    @Published var navigationPath: [ScreenEntry] = []
    @Published var selectedTab: String

    // MARK: - Flow State

    @Published var flowPath: [ScreenEntry] = []
    @Published var flowRootScreen: (any Circuit_runtime_screenScreen)?

    var isFlowActive: Bool { flowRootScreen != nil }

    init(initialTab: String = "home") {
        selectedTab = initialTab
    }

    /// Processes a batch of navigation actions in order.
    /// Actions are batched by `BridgeNavigator` to ensure multiple synchronous navigator calls
    /// (e.g. `pop()` + `goTo()`) are applied in a single SwiftUI update cycle.
    func handle(actions: [NavigationAction]) {
        for action in actions {
            switch action {
            case let presentFlow as NavigationAction.PresentFlow:
                flowRootScreen = presentFlow.screen
                flowPath = []

            case is NavigationAction.DismissFlow:
                flowRootScreen = nil
                flowPath = []

            case let goTo as NavigationAction.GoTo:
                if isFlowActive {
                    flowPath.append(ScreenEntry(screen: goTo.screen))
                } else {
                    navigationPath.append(ScreenEntry(screen: goTo.screen))
                }

            case is NavigationAction.Pop:
                if isFlowActive {
                    if !flowPath.isEmpty {
                        flowPath.removeLast()
                    } else {
                        flowRootScreen = nil
                        flowPath = []
                    }
                } else {
                    if !navigationPath.isEmpty {
                        navigationPath.removeLast()
                    }
                }

            case is NavigationAction.ResetRoot:
                navigationPath.removeAll()
                flowRootScreen = nil
                flowPath = []

            case let switchTab as NavigationAction.SwitchTab:
                navigationPath.removeAll()
                flowRootScreen = nil
                flowPath = []
                selectedTab = switchTab.tag

            case let deepLink as NavigationAction.DeepLink:
                let screens = deepLink.screens as [any Circuit_runtime_screenScreen]
                navigationPath = screens.map { ScreenEntry(screen: $0) }
                flowRootScreen = nil
                flowPath = []

            default:
                break
            }
        }
    }
}
