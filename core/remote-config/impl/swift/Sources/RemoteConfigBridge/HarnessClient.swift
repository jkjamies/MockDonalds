import Foundation
import ff_ios_client_sdk

public final class HarnessListenerToken {
    fileprivate let onCancel: () -> Void
    fileprivate init(onCancel: @escaping () -> Void) { self.onCancel = onCancel }
    public func cancel() { onCancel() }
}

public final class HarnessClient {

    private enum InitState {
        case notStarted
        case inProgress
        case initialized
        case failed
    }

    private let client = CfClient.sharedInstance
    private let apiKey: String
    private let lock = NSLock()
    private var initState: InitState = .notStarted
    private var globalListenerRegistered = false
    private var subscribers: [String: [UUID: () -> Void]] = [:]

    public init(apiKey: String) {
        self.apiKey = apiKey
    }

    public func boolVariation(key: String, defaultValue: Bool) -> Bool {
        ensureInitialized()
        return client.boolVariation(evaluationId: key, defaultValue: defaultValue)
    }

    public func stringVariation(key: String, defaultValue: String) -> String {
        ensureInitialized()
        return client.stringVariation(evaluationId: key, defaultValue: defaultValue)
    }

    public func numberVariation(key: String, defaultValue: Double) -> Double {
        ensureInitialized()
        return client.numberVariation(evaluationId: key, defaultValue: defaultValue)
    }

    // JSON crosses the bridge as a JSON-encoded string; backed by stringVariation
    // so the dashboard stores JSON in a string-typed flag (avoids platform-specific
    // dict types from leaking through the bridge surface).
    public func jsonVariation(key: String, defaultValueJson: String) -> String {
        ensureInitialized()
        return client.stringVariation(evaluationId: key, defaultValue: defaultValueJson)
    }

    // Untyped change listener — fires when *any* flag/config changes; Kotlin then
    // re-fetches the current value via the typed variation methods. Harness iOS
    // SDK 1.3.x has no per-listener unregister, so we keep a single SDK-level "*"
    // listener and fan out to per-key subscriber callbacks; cancelling a Flow
    // removes its UUID from the table without touching the SDK.
    public func registerListener(
        key: String,
        onChange: @escaping () -> Void
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
            self.fanOutChange()
        }
    }

    private func fanOutChange() {
        lock.lock()
        let snapshot = subscribers.values.flatMap { $0.values }
        lock.unlock()
        for callback in snapshot { callback() }
    }

    // Variation reads do not block on init. Until the SDK reports back via the
    // initialize completion handler, Harness returns the local default — the
    // global "*" listener fires once real data lands and observers re-fetch.
    private func ensureInitialized() {
        lock.lock()
        let shouldStart: Bool
        switch initState {
        case .notStarted, .failed:
            initState = .inProgress
            shouldStart = true
        case .inProgress, .initialized:
            shouldStart = false
        }
        lock.unlock()
        guard shouldStart else { return }

        let target = CfTarget.builder().setIdentifier("anonymous").build()
        let config = CfConfiguration.builder().build()
        client.initialize(
            apiKey: apiKey,
            configuration: config,
            target: target
        ) { [weak self] result in
            guard let self = self else { return }
            self.lock.lock()
            if case .success = result {
                self.initState = .initialized
            } else {
                self.initState = .failed
            }
            self.lock.unlock()
            // Wake any observers that subscribed before init landed so they re-fetch
            // the real value via the typed variation methods.
            self.fanOutChange()
        }
    }
}
