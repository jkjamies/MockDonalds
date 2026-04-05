import SwiftUI

/// Provides the Circuit environment to the SwiftUI view hierarchy.
/// Mirrors Android's CircuitCompositionLocals.
struct CircuitStack<Content: View>: View {
    let circuit: CircuitConfiguration
    @ViewBuilder let content: () -> Content

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        content()
            .environment(\.circuitConfiguration, circuit)
            .preferredColorScheme(.dark)
    }
}

// MARK: - Environment Key

private struct CircuitConfigurationKey: EnvironmentKey {
    static let defaultValue = CircuitConfiguration(
        presenterFactories: [],
        uiFactories: []
    )
}

extension EnvironmentValues {
    var circuitConfiguration: CircuitConfiguration {
        get { self[CircuitConfigurationKey.self] }
        set { self[CircuitConfigurationKey.self] = newValue }
    }
}
