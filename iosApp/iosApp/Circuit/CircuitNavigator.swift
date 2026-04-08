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
/// Observes BridgeNavigator's NavigationAction flow and drives native SwiftUI navigation.
/// Also observes overlay requests and presents them as sheets or full-screen covers.
struct CircuitNavigator<Content: View>: View {
    @EnvironmentObject var circuit: CircuitIos

    @State private var navigationPath: [ScreenEntry] = []
    @State private var bottomSheetScreen: ScreenEntry?

    let content: () -> Content

    init(@ViewBuilder content: @escaping () -> Content) {
        self.content = content
    }

    var body: some View {
        NavigationStack(path: $navigationPath) {
            content()
                .navigationDestination(for: ScreenEntry.self) { entry in
                    CircuitContent(screen: entry.screen)
                        .mockDonaldsTheme()
                }
        }
        .sheet(item: $bottomSheetScreen, onDismiss: {
            circuit.navigator.completeBottomSheet(result: BottomSheetResult.Dismissed())
        }) { entry in
            CircuitContent(screen: entry.screen)
                .mockDonaldsTheme()
        }
        .task {
            await observeNavigation()
        }
        .task {
            await observeBottomSheet()
        }
    }

    // MARK: - Navigation Observation

    private func observeNavigation() async {
        do {
            let sequence = asyncSequence(for: circuit.navigator.navigationActionFlow)
            for try await action in sequence {
                handleAction(action)
            }
        } catch {
            print("Navigation observation ended: \(error)")
        }
    }

    // MARK: - Bottom Sheet Observation

    private func observeBottomSheet() async {
        do {
            let sequence = asyncSequence(for: circuit.navigator.bottomSheetRequestFlow)
            for try await request in sequence {
                if let request {
                    bottomSheetScreen = ScreenEntry(screen: request.screen)
                } else {
                    bottomSheetScreen = nil
                }
            }
        } catch {
            print("Bottom sheet observation ended: \(error)")
        }
    }

    // MARK: - Action Handling

    private func handleAction(_ action: NavigationAction) {
        switch action {
        case is NavigationAction.Idle:
            break

        case let goTo as NavigationAction.GoTo:
            navigationPath.append(ScreenEntry(screen: goTo.screen))
            circuit.navigator.consume()

        case is NavigationAction.Pop:
            if !navigationPath.isEmpty {
                navigationPath.removeLast()
            }
            circuit.navigator.consume()

        case is NavigationAction.ResetRoot:
            navigationPath.removeAll()
            circuit.navigator.consume()

        case let deepLink as NavigationAction.DeepLink:
            let screens = deepLink.screens as? [any Circuit_runtime_screenScreen] ?? []
            navigationPath = screens.map { ScreenEntry(screen: $0) }
            circuit.navigator.consume()

        default:
            break
        }
    }
}
