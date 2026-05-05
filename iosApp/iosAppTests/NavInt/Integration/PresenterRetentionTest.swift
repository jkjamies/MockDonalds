import Testing
import ComposeApp
@testable import iosApp

/// Verifies the Phase 1 presenter retention contract on iOS.
///
/// The contract has two halves and this suite exercises both at the integration level:
///   1. SwiftUI's `@StateObject`-backed `CircuitPresenterHolder` keeps the bridge alive
///      for the holder's lifetime and calls `bridge.cancel()` from `deinit` when the
///      holder is released. Test: `holderReleaseCancelsBridge`.
///   2. Each call to `IosApp.presenterBridge(screen:)` produces an INDEPENDENT bridge
///      with its own coroutine scope and `RetainedStateRegistry` — matching Android's
///      record-per-push semantics so two pushes of the same screen do NOT share state.
///      Test: `independentBridgesPerHolder`.
///
/// The pure-Kotlin half (`cancel()` cancels the scope and forgets unclaimed values) is
/// deliberately not duplicated here — it is implicit in the assertions on `bridge.isActive`,
/// which reads `scope.isActive` directly. If `cancel()` regresses, both tests fail.
@Suite @MainActor struct PresenterRetentionTest {

    @Test func holderReleaseCancelsBridge() {
        let app = makeIosApp()
        let bridge = app.presenterBridge(screen: HomeScreen.shared)

        var holder: CircuitPresenterHolder? = CircuitPresenterHolder { bridge }
        _ = holder  // silence unused-warning; the holder retains the bridge for the assertions below

        #expect(bridge.isActive, "Bridge should be active while held by CircuitPresenterHolder")

        holder = nil  // ARC releases the holder → deinit calls bridge.cancel()

        #expect(!bridge.isActive, "Bridge.cancel() must run from holder.deinit when ARC releases the holder")
    }

    @Test func independentBridgesPerHolder() {
        let app = makeIosApp()
        let bridgeA = app.presenterBridge(screen: HomeScreen.shared)
        let bridgeB = app.presenterBridge(screen: HomeScreen.shared)

        var holderA: CircuitPresenterHolder? = CircuitPresenterHolder { bridgeA }
        let holderB = CircuitPresenterHolder { bridgeB }
        _ = holderB  // keep holderB alive through the test scope

        #expect(bridgeA !== bridgeB, "Each presenterBridge call must return a distinct instance")
        #expect(bridgeA.isActive)
        #expect(bridgeB.isActive)

        holderA = nil  // releases only holderA's bridge

        #expect(!bridgeA.isActive, "Releasing holderA must cancel bridgeA")
        #expect(bridgeB.isActive, "Releasing holderA must NOT affect bridgeB — independent scopes")
    }

    // MARK: - Helpers

    private func makeIosApp() -> IosApp {
        IosApp(
            harnessIosBridge: NoOpHarnessIosBridge(),
            akamaiSensorBridge: SwiftAkamaiSensorBridge()
        )
    }
}

/// Test-only HarnessIosBridge that returns default values without initializing the real SDK.
/// The retention test does not exercise feature flags, so canned defaults are sufficient.
private final class NoOpHarnessIosBridge: HarnessIosBridge {
    func boolVariation(key: String, defaultValue: Bool) -> Bool { defaultValue }
    func stringVariation(key: String, defaultValue: String) -> String { defaultValue }
    func longVariation(key: String, defaultValue: Int64) -> Int64 { defaultValue }
    func doubleVariation(key: String, defaultValue: Double) -> Double { defaultValue }
    func jsonVariation(key: String, defaultValueJson: String) -> String { defaultValueJson }
    func registerListener(key: String, onChange: @escaping () -> Void) -> HarnessListenerHandle {
        NoOpHarnessListenerHandle()
    }
}

private final class NoOpHarnessListenerHandle: HarnessListenerHandle {
    func cancel() {}
}
