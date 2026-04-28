package com.mockdonalds.app.features.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.collectAsState
import com.mockdonalds.app.core.centerpost.rememberCenterPost
import com.mockdonalds.app.core.logger.featureLogger
import com.mockdonalds.app.features.login.api.domain.GetLoginContent
import com.mockdonalds.app.features.login.api.navigation.LoginScreen
import com.mockdonalds.app.features.login.api.navigation.WelcomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

private val log = featureLogger("Login")

@CircuitInject(LoginScreen::class, AppScope::class)
@Inject
@Composable
fun LoginPresenter(
    screen: LoginScreen,
    navigator: Navigator,
    authManager: AuthManager,
    getLoginContent: GetLoginContent,
    dispatchers: CenterPostDispatchers,
): LoginUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by getLoginContent.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }

    return LoginUiState(
        logoUrl = content?.logoUrl ?: "",
        email = email,
        eventSink = { event ->
            when (event) {
                is LoginEvent.EmailChanged -> email = event.value
                is LoginEvent.SignInConfirmed -> {
                    log.i { "Sign-in confirmed" }
                    authManager.login()
                    navigator.goTo(WelcomeScreen(returnTo = screen.returnTo))
                }
                is LoginEvent.AppleSignInClicked -> {
                    log.d { "Apple sign-in tapped" }
                    centerPost { }
                }
                is LoginEvent.GoogleSignInClicked -> {
                    log.d { "Google sign-in tapped" }
                    centerPost { }
                }
                is LoginEvent.DismissClicked -> navigator.pop()
            }
        },
    )
}
