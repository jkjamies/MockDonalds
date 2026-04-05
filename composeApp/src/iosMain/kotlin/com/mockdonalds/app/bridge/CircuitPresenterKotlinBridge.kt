package com.mockdonalds.app.bridge

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class CircuitPresenterKotlinBridge<UiState : CircuitUiState>(
    private val presenter: Presenter<UiState>,
    scope: CoroutineScope,
) {
    @NativeCoroutinesState
    val state: StateFlow<UiState> = scope.launchMolecule(
        RecompositionMode.Immediate,
    ) {
        presenter.present()
    }
}
