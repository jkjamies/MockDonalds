package com.mockdonalds.app.features.rewards.presentation

import androidx.compose.runtime.Composable
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope

@CircuitInject(RewardsScreen::class, AppScope::class)
@Composable
fun RewardsPresenter(
    navigator: Navigator,
): RewardsUiState {
    return RewardsUiState(
        eventSink = {},
    )
}
