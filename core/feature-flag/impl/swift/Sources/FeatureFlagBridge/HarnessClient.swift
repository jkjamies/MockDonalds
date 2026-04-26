import Foundation
import ff_ios_client_sdk

public final class HarnessListenerToken {
    fileprivate let onCancel: () -> Void
    fileprivate init(onCancel: @escaping () -> Void) { self.onCancel = onCancel }
    public func cancel() { onCancel() }
}

public final class HarnessClient {

    private let client = CfClient.sharedInstance
    private let apiKey: String
    private let lock = NSLock()
    private var initialized = false
    private var globalListenerRegistered = false
    private var subscribers: [String: [UUID: (Bool) -> Void]] = [:]

    public init(apiKey: String) {
        self.apiKey = apiKey
    }

    public func boolVariation(key: String, defaultValue: Bool) -> Bool {
        ensureInitialized()
        return client.boolVariation(evaluationId: key, defaultValue: defaultValue)
    }

    // Harness iOS SDK (1.3.x) has no per-listener unregister — only a global
    // clearEventsListener(). We register ONE SDK-level listener and fan out to our own
    // per-key subscriber table; cancelling a Flow removes its entry from the table so
    // nothing accumulates.
    public func registerListener(
        key: String,
        onChange: @escaping (Bool) -> Void
    ) -> HarnessListenerToken {
        ensureInitialized()
        ensureGlobalListenerRegistered()

        let id = UUID()
        lock.lock()
        subscribers[key, default: [:]][id] = onChange
        lock.unlock()

        return HarnessListenerToken { [weak self] in
            guard let self = self else { return }
            self.lock.lock()
            self.subscribers[key]?.removeValue(forKey: id)
            if self.subscribers[key]?.isEmpty == true {
                self.subscribers.removeValue(forKey: key)
            }
            self.lock.unlock()
        }
    }

    private func ensureGlobalListenerRegistered() {
        lock.lock()
        guard !globalListenerRegistered else { lock.unlock(); return }
        globalListenerRegistered = true
        lock.unlock()

        client.registerEventsListener(["*"]) { [weak self] result in
            guard let self = self,
                  case .success(let event) = result,
                  case .onEventListener = event else { return }
            self.fanOutCurrentValues()
        }
    }

    private func fanOutCurrentValues() {
        lock.lock()
        let snapshot = subscribers.mapValues { Array($0.values) }
        lock.unlock()

        for (key, callbacks) in snapshot {
            let value = client.boolVariation(evaluationId: key, defaultValue: false)
            for callback in callbacks { callback(value) }
        }
    }

    private func ensureInitialized() {
        lock.lock()
        defer { lock.unlock() }
        guard !initialized else { return }
        let target = CfTarget.builder().setIdentifier("anonymous").build()
        let config = CfConfiguration.builder().build()
        client.initialize(
            apiKey: apiKey,
            configuration: config,
            target: target
        ) { _ in }
        initialized = true
    }
}
