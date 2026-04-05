import SwiftUI

/// Resolves a Screen to its presenter and SwiftUI view.
/// Consumes state from CircuitPresenterKotlinBridge via KMP-NativeCoroutines async/await.
struct CircuitContent: View {
    let screen: any Hashable

    @Environment(\.circuitConfiguration) private var circuit

    var body: some View {
        // Placeholder — will resolve presenter + view from circuit registry
        // once the shared framework bridge is fully wired.
        Text("Screen: \(String(describing: screen))")
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color(hex: 0x131313))
    }
}

// MARK: - Hex Color Extension

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}
