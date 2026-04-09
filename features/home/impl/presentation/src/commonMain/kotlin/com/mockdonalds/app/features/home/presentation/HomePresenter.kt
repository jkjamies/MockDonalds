package com.mockdonalds.app.features.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.collectAsState
import com.mockdonalds.app.core.centerpost.rememberCenterPost
import com.mockdonalds.app.features.home.api.domain.GetHomeContent
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
@Composable
fun HomePresenter(
    navigator: Navigator,
    getHomeContent: GetHomeContent,
    dispatchers: CenterPostDispatchers,
): HomeUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by getHomeContent.collectAsState()

    return HomeUiState(
        userName = content?.userName ?: "",
        heroPromotion = content?.heroPromotion,
        recentCravings = content?.recentCravings ?: emptyList(),
        exploreItems = content?.exploreItems ?: emptyList(),
        eventSink = { event ->
            when (event) {
                is HomeEvent.HeroCtaClicked -> centerPost { }
                is HomeEvent.CravingClicked -> centerPost { }
                is HomeEvent.ExploreItemClicked -> centerPost { }
            }
        },
    )
}
