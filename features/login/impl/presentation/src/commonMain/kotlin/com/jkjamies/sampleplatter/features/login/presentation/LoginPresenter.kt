package com.jkjamies.sampleplatter.features.login.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.jkjamies.sampleplatter.core.auth.AuthManager
import com.jkjamies.sampleplatter.core.logger.featureLogger
import com.jkjamies.sampleplatter.core.presentation.strata.collectAsState
import com.jkjamies.sampleplatter.core.presentation.strata.rememberStrata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
import com.jkjamies.sampleplatter.features.login.api.domain.GetLoginContent
import com.jkjamies.sampleplatter.features.login.api.navigation.LoginScreen
import com.jkjamies.sampleplatter.features.login.api.navigation.WelcomeScreen
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
    dispatchers: StrataDispatchers,
): LoginUiState {
    val strata = rememberStrata(dispatchers)
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
                    strata { }
                }
                is LoginEvent.GoogleSignInClicked -> {
                    log.d { "Google sign-in tapped" }
                    strata { }
                }
                is LoginEvent.DismissClicked -> navigator.pop()
            }
        },
    )
}
