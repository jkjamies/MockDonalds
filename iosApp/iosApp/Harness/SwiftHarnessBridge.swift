import Foundation
import ComposeApp
import FeatureFlagBridge

final class SwiftHarnessBridge: HarnessIosBridge {

    private let client = HarnessClient(
        apiKey: FeatureFlagBuildConfig.shared.HARNESS_CLIENT_ID
    )

    func boolVariation(key: String, defaultValue: Bool) -> Bool {
        client.boolVariation(key: key, defaultValue: defaultValue)
    }

    func registerListener(
        key: String,
        onChange: @escaping (KotlinBoolean) -> Void
    ) -> HarnessListenerHandle {
        let token = client.registerListener(key: key) { value in
            onChange(KotlinBoolean(bool: value))
        }
        return SwiftHarnessListenerHandle { token.cancel() }
    }
}

private final class SwiftHarnessListenerHandle: HarnessListenerHandle {
    private let onCancel: () -> Void
    init(onCancel: @escaping () -> Void) { self.onCancel = onCancel }
    func cancel() { onCancel() }
}
