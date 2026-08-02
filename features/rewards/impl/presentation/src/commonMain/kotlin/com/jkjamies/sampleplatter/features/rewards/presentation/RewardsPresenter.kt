package com.jkjamies.sampleplatter.features.rewards.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.centerpost.CenterPostDispatchers
import com.jkjamies.sampleplatter.core.presentation.centerpost.collectAsState
import com.jkjamies.sampleplatter.core.presentation.centerpost.rememberCenterPost
import com.jkjamies.sampleplatter.features.rewards.api.domain.GetRewardsContent
import com.jkjamies.sampleplatter.features.rewards.api.navigation.RewardsScreen
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
