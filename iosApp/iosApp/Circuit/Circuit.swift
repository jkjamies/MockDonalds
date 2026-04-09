import SwiftUI
import ComposeApp

final class CircuitIos: ObservableObject {

    protocol UiFactory {
        func create(screen: any Circuit_runtime_screenScreen) -> ((any Circuit_runtimeCircuitUiState) -> AnyView)?
    }

    let iosApp: IosApp
    private let uiFactories: [UiFactory]

    var navigator: BridgeNavigator { iosApp.navigator }

    init(
        iosApp: IosApp,
        uiFactories: [UiFactory]
    ) {
        self.iosApp = iosApp
        self.uiFactories = uiFactories
    }

    func presenter(
        screen: any Circuit_runtime_screenScreen
    ) -> CircuitPresenterKotlinBridge<any Circuit_runtimeCircuitUiState> {
        return iosApp.presenterBridge(screen: screen)
    }

    func ui(screen: any Circuit_runtime_screenScreen) -> (any Circuit_runtimeCircuitUiState) -> AnyView {
        for factory in uiFactories {
            if let ui = factory.create(screen: screen) {
                return ui
            }
        }
        return { _ in AnyView(Text("No UI for screen: \(String(describing: type(of: screen)))")) }
    }
}

extension CircuitIos.UiFactory {
    func ui<S: Circuit_runtimeCircuitUiState>(
        @ViewBuilder _ viewBuilder: @escaping (S) -> some View
    ) -> ((any Circuit_runtimeCircuitUiState) -> AnyView) {
        return { state in AnyView(viewBuilder(state as! S)) }
    }
}

/// Generic factory that eliminates the need for per-screen UiFactory classes.
/// Usage: `ScreenUiFactory<HomeScreen, HomeUiState> { HomeView(state: $0) }`
final class ScreenUiFactory<S: Circuit_runtime_screenScreen, State: Circuit_runtimeCircuitUiState>: CircuitIos.UiFactory {
    private let viewBuilder: (State) -> AnyView

    init(@ViewBuilder _ viewBuilder: @escaping (State) -> some View) {
        self.viewBuilder = { state in AnyView(viewBuilder(state)) }
    }

    func create(screen: any Circuit_runtime_screenScreen) -> ((any Circuit_runtimeCircuitUiState) -> AnyView)? {
        guard screen is S else { return nil }
        return { state in self.viewBuilder(state as! State) }
    }
}
