import SwiftUI
import ComposeApp
import KMPNativeCoroutinesAsync

struct CircuitView: View {
    @State private var state: (any Circuit_runtimeCircuitUiState)?

    private let presenter: CircuitPresenterKotlinBridge<any Circuit_runtimeCircuitUiState>
    private var content: (any Circuit_runtimeCircuitUiState) -> AnyView

    init(
        _ presenter: @autoclosure @escaping () -> CircuitPresenterKotlinBridge<any Circuit_runtimeCircuitUiState>,
        _ content: @escaping (any Circuit_runtimeCircuitUiState) -> AnyView
    ) {
        self.presenter = presenter()
        self.content = content
    }

    var body: some View {
        ZStack {
            if let state = self.state {
                content(state)
            } else {
                ProgressView()
                    .tint(.white)
            }
        }
        .task {
            do {
                let sequence = asyncSequence(for: presenter.stateFlow)
                for try await state in sequence {
                    self.state = state
                }
            } catch {
                print("State observation ended: \(error)")
            }
        }
    }
}
