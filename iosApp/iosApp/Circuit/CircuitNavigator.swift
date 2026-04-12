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
/// Navigation logic is delegated to `NavigationStateManager` for testability.
struct CircuitNavigator<Content: View>: View {
    @EnvironmentObject var circuit: CircuitIos

    @StateObject private var stateManager: NavigationStateManager
    @Binding var selectedTab: String

    let content: () -> Content

    init(selectedTab: Binding<String>, @ViewBuilder content: @escaping () -> Content) {
        self._selectedTab = selectedTab
        self._stateManager = StateObject(wrappedValue: NavigationStateManager(initialTab: selectedTab.wrappedValue))
        self.content = content
    }

    var body: some View {
        NavigationStack(path: $stateManager.navigationPath) {
            content()
                .navigationDestination(for: ScreenEntry.self) { entry in
                    CircuitContent(screen: entry.screen)
                        .mockDonaldsTheme()
                        .id(entry.id)
                }
        }
        .fullScreenCover(isPresented: Binding(
            get: { stateManager.isFlowActive },
            set: { if !$0 { stateManager.flowRootScreen = nil; stateManager.flowPath = [] } }
        )) {
            if let rootScreen = stateManager.flowRootScreen {
                NavigationStack(path: $stateManager.flowPath) {
                    CircuitContent(screen: rootScreen)
                        .mockDonaldsTheme()
                        .navigationDestination(for: ScreenEntry.self) { entry in
                            CircuitContent(screen: entry.screen)
                                .mockDonaldsTheme()
                                .id(entry.id)
                        }
                }
                .interactiveDismissDisabled()
            }
        }
        .task {
            await observeNavigation()
        }
        .onReceive(stateManager.$selectedTab) { newValue in
            selectedTab = newValue
        }
    }

    // MARK: - Navigation Observation

    private func observeNavigation() async {
        do {
            let sequence = asyncSequence(for: circuit.navigator.navigationActions)
            for try await actions in sequence {
                stateManager.handle(actions: actions)
            }
        } catch {
            print("Navigation observation ended: \(error)")
        }
    }
}
