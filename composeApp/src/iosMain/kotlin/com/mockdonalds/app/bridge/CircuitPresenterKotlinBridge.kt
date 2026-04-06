package com.mockdonalds.app.bridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.currentComposer
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.rememberRetainedStateRegistry
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.StateFlow

class CircuitPresenterKotlinBridge<UiState : CircuitUiState>(
    private val presenter: Presenter<UiState>,
    scope: CoroutineScope,
) {
    constructor(presenter: Presenter<UiState>) : this(presenter, MainScope())

    @NativeCoroutinesState
    val state: StateFlow<UiState> = scope.launchMolecule(
        RecompositionMode.Immediate,
    ) {
        val retainedStateRegistry = rememberRetainedStateRegistry()

        withCompositionLocalProvider(
            LocalRetainedStateRegistry provides retainedStateRegistry,
        ) {
            presenter.present()
        }
    }
}

@OptIn(InternalComposeApi::class)
@Composable
private fun <R> withCompositionLocalProvider(
    vararg values: ProvidedValue<*>,
    content: @Composable () -> R,
): R {
    currentComposer.startProviders(values)
    return content().also { currentComposer.endProviders() }
}
