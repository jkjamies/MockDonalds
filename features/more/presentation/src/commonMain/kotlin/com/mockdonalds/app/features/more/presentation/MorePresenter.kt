package com.mockdonalds.app.features.more.presentation

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Inject

@CircuitInject(MoreScreen::class, AppScope::class)
@Inject
class MorePresenter(
    private val navigator: Navigator,
) : Presenter<MoreUiState> {

    @Composable
    override fun present(): MoreUiState {
        return MoreUiState(
            eventSink = {},
        )
    }
}
