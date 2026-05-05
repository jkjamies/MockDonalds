import SwiftUI
import ComposeApp
import KMPNativeCoroutinesAsync

@MainActor
final class CircuitPresenterHolder: ObservableObject {
    let presenter: CircuitPresenterKotlinBridge<any Circuit_runtimeCircuitUiState>

    init(_ make: () -> CircuitPresenterKotlinBridge<any Circuit_runtimeCircuitUiState>) {
        self.presenter = make()
    }

    deinit {
        presenter.cancel()
    }
}

struct CircuitView: View {
    @StateObject private var holder: CircuitPresenterHolder
    @State private var state: (any Circuit_runtimeCircuitUiState)?
    private let content: (any Circuit_runtimeCircuitUiState) -> AnyView

    init(
        _ presenter: @autoclosure @escaping () -> CircuitPresenterKotlinBridge<any Circuit_runtimeCircuitUiState>,
        _ content: @escaping (any Circuit_runtimeCircuitUiState) -> AnyView
    ) {
        _holder = StateObject(wrappedValue: CircuitPresenterHolder(presenter))
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
                let sequence = asyncSequence(for: holder.presenter.stateFlow)
                for try await state in sequence {
                    self.state = state
                }
            } catch {
                print("State observation ended: \(error)")
            }
        }
    }
}
