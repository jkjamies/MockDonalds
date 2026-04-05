package com.mockdonalds.app.features.rewards.presentation

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Inject

@CircuitInject(RewardsScreen::class, AppScope::class)
@Inject
class RewardsPresenter(
    private val navigator: Navigator,
) : Presenter<RewardsUiState> {

    @Composable
    override fun present(): RewardsUiState {
        return RewardsUiState(
            eventSink = {},
        )
    }
}
