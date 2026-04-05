package com.mockdonalds.app.features.home.presentation

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Inject

@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
class HomePresenter(
    private val navigator: Navigator,
) : Presenter<HomeUiState> {

    @Composable
    override fun present(): HomeUiState {
        return HomeUiState(
            eventSink = {},
        )
    }
}
