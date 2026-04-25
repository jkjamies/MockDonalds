import SwiftUI
import ComposeApp
import FeatureFlagBridge

final class AppDelegate: NSObject, UIApplicationDelegate {

    lazy var circuit: CircuitIos = {
        let iosApp = IosApp(
            harnessIosBridge: SwiftHarnessBridge(),
            akamaiSensorBridge: SwiftAkamaiSensorBridge()
        )
        return CircuitIos(iosApp: iosApp, uiFactories: CircuitIos.generatedFactories())
    }()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        #if DEBUG
        // XCUITest hook: e2e journey tests pass DEEP_LINK_URI via launchEnvironment
        // to exercise deeplink flows without routing through Safari/system UI.
        // No-op in production — the env var is never set outside the test harness.
        if let uri = ProcessInfo.processInfo.environment["DEEP_LINK_URI"],
           let url = URL(string: uri) {
            DispatchQueue.main.async { [weak self] in
                self?.handleDeepLink(url: url)
            }
        }
        #endif
        return true
    }

    func handleDeepLink(url: URL) {
        circuit.iosApp.deepLink(uri: url.absoluteString)
    }
}
