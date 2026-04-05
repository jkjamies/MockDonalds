package com.mockdonalds.app.features.splash.presentation

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.mockdonalds.app.features.splash.api.navigation.SplashScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Inject

@CircuitInject(SplashScreen::class, AppScope::class)
@Inject
class SplashPresenter(
    private val navigator: Navigator,
) : Presenter<SplashUiState> {

    @Composable
    override fun present(): SplashUiState {
        return SplashUiState(
            eventSink = { event ->
                when (event) {
                    SplashEvent.AnimationComplete -> {
                        navigator.resetRoot(HomeScreen)
                    }
                }
            },
        )
    }
}
