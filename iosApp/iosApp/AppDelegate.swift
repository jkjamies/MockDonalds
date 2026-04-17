import SwiftUI
import ComposeApp

final class AppDelegate: NSObject, UIApplicationDelegate {

    lazy var circuit: CircuitIos = {
        let iosApp = IosApp()
        return CircuitIos(
            iosApp: iosApp,
            uiFactories: [
                ScreenUiFactory<HomeScreen, HomeUiState> { HomeView(state: $0) },
                ScreenUiFactory<OrderScreen, OrderUiState> { OrderView(state: $0) },
                ScreenUiFactory<RewardsScreen, RewardsUiState> { RewardsView(state: $0) },
                ScreenUiFactory<ScanScreen, ScanUiState> { ScanView(state: $0) },
                ScreenUiFactory<MoreScreen, MoreUiState> { MoreView(state: $0) },
                ScreenUiFactory<RecentsScreen, RecentsUiState> { RecentsView(state: $0) },
                ScreenUiFactory<LoginScreen, LoginUiState> { LoginView(state: $0) },
                ScreenUiFactory<ProfileScreen, ProfileUiState> { ProfileView(state: $0) },
                ScreenUiFactory<WelcomeScreen, WelcomeUiState> { WelcomeView(state: $0) },
            ]
        )
    }()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        return true
    }

    func handleDeepLink(url: URL) {
        circuit.iosApp.deepLink(uri: url.absoluteString)
    }
}
