import SwiftUI
import ComposeApp

struct CircuitContent: View {
    @EnvironmentObject var circuit: CircuitIos

    private let screen: any Circuit_runtime_screenScreen

    init(screen: any Circuit_runtime_screenScreen) {
        self.screen = screen
    }

    var body: some View {
        CircuitView(
            circuit.presenter(screen: screen),
            circuit.ui(screen: screen)
        )
    }
}
