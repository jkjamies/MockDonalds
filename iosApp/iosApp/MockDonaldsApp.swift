import SwiftUI

@main
struct MockDonaldsApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            CircuitStack(circuit: delegate.circuit) {
                CircuitContent(screen: delegate.splashScreen)
            }
        }
    }
}
