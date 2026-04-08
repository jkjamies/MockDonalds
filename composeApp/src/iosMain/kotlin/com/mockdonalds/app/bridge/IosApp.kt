package com.mockdonalds.app.bridge

import com.mockdonalds.app.AppGraph
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.zacsweers.metro.createGraph

class IosApp {
    private val graph = createGraph<AppGraph>()

    val circuit: Circuit get() = graph.circuit
    val navigator = BridgeNavigator()

    fun presenterBridge(screen: Screen): CircuitPresenterKotlinBridge<CircuitUiState> {
        val presenter = circuit.presenter(screen, navigator)
            ?: error("No presenter found for screen: $screen")
        @Suppress("UNCHECKED_CAST")
        return CircuitPresenterKotlinBridge(
            presenter = presenter as Presenter<CircuitUiState>,
            bottomSheetNavigator = navigator,
        )
    }
}
