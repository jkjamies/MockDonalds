import Foundation
import ComposeApp
import RemoteConfigBridge

final class SwiftHarnessBridge: HarnessIosBridge {

    private let client = HarnessClient(
        apiKey: RemoteConfigBuildConfig.shared.HARNESS_CLIENT_ID
    )

    func boolVariation(key: String, defaultValue: Bool) -> Bool {
        client.boolVariation(key: key, defaultValue: defaultValue)
    }

    func stringVariation(key: String, defaultValue: String) -> String {
        client.stringVariation(key: key, defaultValue: defaultValue)
    }

    func longVariation(key: String, defaultValue: Int64) -> Int64 {
        // Routed through stringVariation (not numberVariation) to preserve full
        // 64-bit precision — Harness numberVariation is Double-typed and loses
        // precision for values above 2^53.
        Int64(client.stringVariation(key: key, defaultValue: String(defaultValue))) ?? defaultValue
    }

    func doubleVariation(key: String, defaultValue: Double) -> Double {
        client.numberVariation(key: key, defaultValue: defaultValue)
    }

    func jsonVariation(key: String, defaultValueJson: String) -> String {
        client.jsonVariation(key: key, defaultValueJson: defaultValueJson)
    }

    func registerListener(
        key: String,
        onChange: @escaping () -> Void
    ) -> HarnessListenerHandle {
        let token = client.registerListener(key: key, onChange: onChange)
        return SwiftHarnessListenerHandle { token.cancel() }
    }
}

private final class SwiftHarnessListenerHandle: HarnessListenerHandle {
    private let onCancel: () -> Void
    init(onCancel: @escaping () -> Void) { self.onCancel = onCancel }
    func cancel() { onCancel() }
}
