package com.jkjamies.sampleplatter.features.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.presentation.strata.collectAsState
import com.jkjamies.sampleplatter.core.presentation.strata.rememberStrata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
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
    dispatchers: StrataDispatchers,
): HomeUiState {
    val strata = rememberStrata(dispatchers)
    val content by getHomeContent.collectAsState()

    return HomeUiState(
        userName = content?.userName ?: "",
        heroPromotion = content?.heroPromotion,
        recentCravings = content?.recentCravings ?: emptyList(),
        exploreItems = content?.exploreItems ?: emptyList(),
        eventSink = { event ->
            when (event) {
                is HomeEvent.HeroCtaClicked -> strata { }
                is HomeEvent.CravingClicked -> strata { }
                is HomeEvent.ExploreItemClicked -> strata { }
            }
        },
    )
}
