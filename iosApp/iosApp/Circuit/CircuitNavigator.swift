import SwiftUI
import ComposeApp
import KMPNativeCoroutinesAsync

/// A hashable wrapper around Circuit screens for use with NavigationStack.
/// Each entry has a unique ID so the same screen type can appear multiple times in the stack.
struct ScreenEntry: Hashable, Identifiable {
    let id = UUID()
    let screen: any Circuit_runtime_screenScreen

    static func == (lhs: ScreenEntry, rhs: ScreenEntry) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

/// The iOS equivalent of Android's NavigableCircuitContent.
/// Observes BridgeNavigator's batched navigation actions and drives native SwiftUI navigation.
struct CircuitNavigator<Content: View>: View {
    @EnvironmentObject var circuit: CircuitIos

    @State private var navigationPath: [ScreenEntry] = []
    @Binding var selectedTab: String

    let content: () -> Content

    init(selectedTab: Binding<String>, @ViewBuilder content: @escaping () -> Content) {
        self._selectedTab = selectedTab
        self.content = content
    }

    var body: some View {
        NavigationStack(path: $navigationPath) {
            content()
                .navigationDestination(for: ScreenEntry.self) { entry in
                    CircuitContent(screen: entry.screen)
                        .mockDonaldsTheme()
                        .id(entry.id)
                }
        }
        .task {
            await observeNavigation()
        }
    }

    // MARK: - Navigation Observation

    private func observeNavigation() async {
        do {
            let sequence = asyncSequence(for: circuit.navigator.navigationActions)
            for try await actions in sequence {
                handleActions(actions)
            }
        } catch {
            print("Navigation observation ended: \(error)")
        }
    }

    // MARK: - Action Handling

    private func handleActions(_ actions: [NavigationAction]) {
        for action in actions {
            switch action {
            case let goTo as NavigationAction.GoTo:
                navigationPath.append(ScreenEntry(screen: goTo.screen))

            case is NavigationAction.Pop:
                if !navigationPath.isEmpty {
                    navigationPath.removeLast()
                }

            case is NavigationAction.ResetRoot:
                navigationPath.removeAll()

            case let switchTab as NavigationAction.SwitchTab:
                navigationPath.removeAll()
                selectedTab = switchTab.tag

            case let deepLink as NavigationAction.DeepLink:
                let screens = deepLink.screens as [any Circuit_runtime_screenScreen]
                navigationPath = screens.map { ScreenEntry(screen: $0) }

            default:
                break
            }
        }
    }
}
