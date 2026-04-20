import SwiftUI
import ComposeApp
import FeatureFlagBridge

final class AppDelegate: NSObject, UIApplicationDelegate {

    lazy var circuit: CircuitIos = {
        let iosApp = IosApp(harnessIosBridge: SwiftHarnessBridge())

        var factories: [CircuitIos.UiFactory] = [
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

        #if DEBUG
        factories.append(ScreenUiFactory<DebugMenuScreen, DebugMenuUiState> { DebugMenuView(state: $0) })
        factories.append(ScreenUiFactory<FeatureFlagsDebugScreen, FeatureFlagsDebugUiState> { FeatureFlagsDebugView(state: $0) })
        factories.append(ScreenUiFactory<BuildConfigDebugScreen, BuildConfigDebugUiState> { BuildConfigDebugView(state: $0) })
        #endif

        return CircuitIos(iosApp: iosApp, uiFactories: factories)
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
