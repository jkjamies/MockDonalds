import SwiftUI
import ComposeApp

@main
struct MockDonaldsApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @State private var selectedTab = "home"

    var body: some Scene {
        WindowGroup {
            CircuitStack(delegate.circuit) {
                CircuitNavigator(selectedTab: $selectedTab) {
                    TabView(selection: $selectedTab) {
                        CircuitContent(screen: HomeScreen.shared)
                            .tabItem { Label("HOME", systemImage: "house.fill") }
                            .tag(HomeScreen.shared.tag)
                        CircuitContent(screen: OrderScreen.shared)
                            .tabItem { Label("ORDER", systemImage: "menucard.fill") }
                            .tag(OrderScreen.shared.tag)
                        CircuitContent(screen: RewardsScreen.shared)
                            .tabItem { Label("REWARDS", systemImage: "star.fill") }
                            .tag(RewardsScreen.shared.tag)
                        CircuitContent(screen: ScanScreen.shared)
                            .tabItem { Label("SCAN", systemImage: "qrcode") }
                            .tag(ScanScreen.shared.tag)
                        CircuitContent(screen: MoreScreen.shared)
                            .tabItem { Label("MORE", systemImage: "ellipsis") }
                            .tag(MoreScreen.shared.tag)
                    }
                    .tint(MockDonaldsColors.secondary)
                    .mockDonaldsTheme()
                }
            }
            .overlay(alignment: .topTrailing) {
                Text("\(delegate.circuit.iosApp.market.uppercased())/\(delegate.circuit.iosApp.env)")
                    .padding(.top, 4)
                    .padding(.trailing, 8)
                    .allowsHitTesting(false)
            }
            .onOpenURL { url in
                delegate.handleDeepLink(url: url)
            }
        }
    }
}
