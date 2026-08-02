package com.jkjamies.sampleplatter.features.profile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.auth.AuthManager
import com.jkjamies.sampleplatter.core.presentation.strata.collectAsState
import com.jkjamies.sampleplatter.core.presentation.strata.rememberStrata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
import com.jkjamies.sampleplatter.features.profile.api.domain.GetProfileContent
import com.jkjamies.sampleplatter.features.profile.api.navigation.ProfileScreen
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
    dispatchers: StrataDispatchers,
): ProfileUiState {
    rememberStrata(dispatchers)
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
