package com.jkjamies.sampleplatter.features.more.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfig
import com.jkjamies.sampleplatter.core.buildconfig.isDebug
import com.jkjamies.sampleplatter.core.presentation.strata.collectAsState
import com.jkjamies.sampleplatter.core.presentation.strata.rememberStrata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
import com.jkjamies.sampleplatter.features.more.api.domain.GetMoreContent
import com.jkjamies.sampleplatter.features.more.api.domain.MoreMenuItem
import com.jkjamies.sampleplatter.features.more.api.navigation.MoreScreen
import com.jkjamies.sampleplatter.features.more.api.navigation.MoreTabExtension
import com.jkjamies.sampleplatter.features.nutrition.api.navigation.NutritionScreen
import com.jkjamies.sampleplatter.features.profile.api.navigation.ProfileScreen
import com.jkjamies.sampleplatter.features.recents.api.navigation.RecentsScreen
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
    dispatchers: StrataDispatchers,
    tabExtensions: Set<MoreTabExtension>,
    buildConfig: AppBuildConfig,
): MoreUiState {
    rememberStrata(dispatchers)
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
                    } else {
                        when (event.id) {
                            "recents" -> navigator.goTo(RecentsScreen)
                            "nutrition" -> navigator.goTo(NutritionScreen)
                        }
                    }
                }
            }
        },
    )
}
