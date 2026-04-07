package com.mockdonalds.app.features.rewards.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.collectAsState
import com.mockdonalds.app.core.centerpost.rememberCenterPost
import com.mockdonalds.app.features.rewards.api.domain.GetRewardsContent
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(RewardsScreen::class, AppScope::class)
@Inject
@Composable
fun RewardsPresenter(
    navigator: Navigator,
    getRewardsContent: GetRewardsContent,
    dispatchers: CenterPostDispatchers,
): RewardsUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by getRewardsContent.collectAsState()

    return RewardsUiState(
        progress = content?.progress,
        vaultSpecials = content?.vaultSpecials ?: emptyList(),
        history = content?.history ?: emptyList(),
        eventSink = { event ->
            when (event) {
                is RewardsEvent.VaultSpecialClicked -> centerPost { }
                is RewardsEvent.ViewAllClicked -> centerPost { }
            }
        },
    )
}
