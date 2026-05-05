package com.mockdonalds.app.bridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.currentComposer
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.RetainedStateRegistry
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

class CircuitPresenterKotlinBridge<UiState : CircuitUiState>(
    private val presenter: Presenter<UiState>,
    private val scope: CoroutineScope = MainScope(),
) {
    private val retainedStateRegistry = RetainedStateRegistry()

    @NativeCoroutinesState
    val state: StateFlow<UiState> = scope.launchMolecule(
        RecompositionMode.Immediate,
    ) {
        withCompositionLocalProvider(
            LocalRetainedStateRegistry provides retainedStateRegistry,
        ) {
            presenter.present()
        }
    }

    fun cancel() {
        retainedStateRegistry.forgetUnclaimedValues()
        scope.cancel()
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
