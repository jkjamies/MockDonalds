package com.mockdonalds.app.features.scan.presentation

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import com.mockdonalds.app.features.scan.api.navigation.ScanScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Inject

@CircuitInject(ScanScreen::class, AppScope::class)
@Inject
class ScanPresenter(
    private val navigator: Navigator,
) : Presenter<ScanUiState> {

    @Composable
    override fun present(): ScanUiState {
        return ScanUiState(
            eventSink = {},
        )
    }
}
