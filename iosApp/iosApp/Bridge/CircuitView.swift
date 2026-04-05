import SwiftUI

/// Generic view that observes a Circuit presenter's state via KMP-NativeCoroutines.
///
/// Usage (once bridge is wired):
/// ```swift
/// CircuitView(presenter: homeBridge) { state in
///     HomeScreenView(state: state)
/// }
/// ```
struct CircuitView<State, Content: View>: View {
    @SwiftUI.State private var state: State?
    private let stateStream: AsyncStream<State>
    private let content: (State) -> Content

    init(
        stateStream: AsyncStream<State>,
        @ViewBuilder content: @escaping (State) -> Content
    ) {
        self.stateStream = stateStream
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
        .task { @MainActor in
            for await newState in stateStream {
                self.state = newState
            }
        }
    }
}
