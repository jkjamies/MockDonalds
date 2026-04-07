package com.mockdonalds.app.features.more.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.collectAsState
import com.mockdonalds.app.core.centerpost.rememberCenterPost
import com.mockdonalds.app.features.more.api.domain.GetMoreContent
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(MoreScreen::class, AppScope::class)
@Inject
@Composable
fun MorePresenter(
    navigator: Navigator,
    getMoreContent: GetMoreContent,
    dispatchers: CenterPostDispatchers,
): MoreUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by getMoreContent.collectAsState()

    return MoreUiState(
        userProfile = content?.userProfile,
        menuItems = content?.menuItems ?: emptyList(),
        eventSink = { event ->
            when (event) {
                is MoreEvent.ProfileClicked -> centerPost { }
                is MoreEvent.MenuItemClicked -> centerPost { }
            }
        },
    )
}
