package com.jkjamies.sampleplatter.features.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.centerpost.CenterPostDispatchers
import com.jkjamies.sampleplatter.core.presentation.centerpost.collectAsState
import com.jkjamies.sampleplatter.core.presentation.centerpost.rememberCenterPost
import com.jkjamies.sampleplatter.features.home.api.domain.GetHomeContent
import com.jkjamies.sampleplatter.features.home.api.navigation.HomeScreen
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
