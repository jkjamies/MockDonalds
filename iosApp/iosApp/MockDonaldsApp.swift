import SwiftUI

@main
struct MockDonaldsApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            CircuitStack(circuit: delegate.circuit) {
                CircuitContent(screen: HomeScreenPlaceholder())
            }
        }
    }
}

/// Placeholder until CMP framework exports HomeScreen
private struct HomeScreenPlaceholder: Hashable {}
