package com.mockdonalds.app.features.profile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.collectAsState
import com.mockdonalds.app.core.centerpost.rememberCenterPost
import com.mockdonalds.app.features.profile.api.domain.GetProfileContent
import com.mockdonalds.app.features.profile.api.navigation.ProfileScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(ProfileScreen::class, AppScope::class)
@Inject
@Composable
fun ProfilePresenter(
    navigator: Navigator,
    authManager: AuthManager,
    getProfileContent: GetProfileContent,
    dispatchers: CenterPostDispatchers,
): ProfileUiState {
    rememberCenterPost(dispatchers)
    val content by getProfileContent.collectAsState()

    return ProfileUiState(
        name = content?.name ?: "",
        email = content?.email ?: "",
        tier = content?.tier ?: "",
        points = content?.points ?: "",
        avatarUrl = content?.avatarUrl ?: "",
        memberSince = content?.memberSince ?: "",
        eventSink = { event ->
            when (event) {
                is ProfileEvent.LogoutClicked -> {
                    authManager.logout()
                    navigator.pop()
                }
            }
        },
    )
}
