import SwiftUI

struct CircuitStack<Content>: View where Content: View {
    private var content: () -> Content
    private var circuit: CircuitIos

    init(_ circuit: CircuitIos, @ViewBuilder _ content: @escaping () -> Content) {
        self.content = content
        self.circuit = circuit
    }

    var body: some View {
        content()
            .environmentObject(circuit)
            .preferredColorScheme(.dark)
    }
}
