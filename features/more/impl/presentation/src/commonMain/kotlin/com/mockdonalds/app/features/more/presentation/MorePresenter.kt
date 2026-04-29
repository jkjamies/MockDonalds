package com.mockdonalds.app.features.more.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.core.buildconfig.isDebug
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.presentation.centerpost.collectAsState
import com.mockdonalds.app.core.presentation.centerpost.rememberCenterPost
import com.mockdonalds.app.features.more.api.domain.GetMoreContent
import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.mockdonalds.app.features.more.api.navigation.MoreTabExtension
import com.mockdonalds.app.features.profile.api.navigation.ProfileScreen
import com.mockdonalds.app.features.recents.api.navigation.RecentsScreen
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
    tabExtensions: Set<MoreTabExtension>,
    buildConfig: AppBuildConfig,
): MoreUiState {
    rememberCenterPost(dispatchers)
    val content by getMoreContent.collectAsState()

    val visibleExtensions = tabExtensions.filter { buildConfig.isDebug || !it.isDebugOnly }
    val extensionItems = visibleExtensions.map { ext ->
        MoreMenuItem(id = ext.id, icon = ext.icon, title = ext.title)
    }
    val extensionsById = visibleExtensions.associateBy { it.id }

    return MoreUiState(
        userProfile = content?.userProfile,
        menuItems = (content?.menuItems ?: emptyList()) + extensionItems,
        eventSink = { event ->
            when (event) {
                is MoreEvent.ProfileClicked -> navigator.goTo(ProfileScreen)
                is MoreEvent.MenuItemClicked -> {
                    val extension = extensionsById[event.id]
                    if (extension != null) {
                        extension.onClick(navigator)
                    } else if (event.id == "1") {
                        navigator.goTo(RecentsScreen)
                    }
                }
            }
        },
    )
}
