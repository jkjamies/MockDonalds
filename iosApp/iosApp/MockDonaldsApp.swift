import SwiftUI
import ComposeApp

@main
struct MockDonaldsApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            CircuitStack(delegate.circuit) {
                TabView {
                    CircuitContent(screen: HomeScreen.shared)
                        .tabItem { Label("HOME", systemImage: "house.fill") }
                    CircuitContent(screen: OrderScreen.shared)
                        .tabItem { Label("ORDER", systemImage: "menucard.fill") }
                    CircuitContent(screen: RewardsScreen.shared)
                        .tabItem { Label("REWARDS", systemImage: "star.fill") }
                    CircuitContent(screen: ScanScreen.shared)
                        .tabItem { Label("SCAN", systemImage: "qrcode") }
                    CircuitContent(screen: MoreScreen.shared)
                        .tabItem { Label("MORE", systemImage: "ellipsis") }
                }
                .tint(MockDonaldsColors.secondary)
            }
        }
    }
}
