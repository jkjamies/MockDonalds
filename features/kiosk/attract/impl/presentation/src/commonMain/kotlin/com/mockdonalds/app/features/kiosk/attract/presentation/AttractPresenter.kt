package com.mockdonalds.app.features.kiosk.attract.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.presentation.centerpost.collectAsState
import com.mockdonalds.app.core.presentation.centerpost.rememberCenterPost
import com.mockdonalds.app.features.kiosk.attract.api.domain.GetAttractContent
import com.mockdonalds.app.features.kiosk.attract.api.navigation.AttractScreen
import com.mockdonalds.app.features.kiosk.identify.api.navigation.IdentifyScreen
import com.mockdonalds.app.features.kiosk.order.api.navigation.KioskOrderScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(AttractScreen::class, AppScope::class)
@Inject
@Composable
fun AttractPresenter(
    navigator: Navigator,
    getAttractContent: GetAttractContent,
    dispatchers: CenterPostDispatchers,
): AttractUiState {
    rememberCenterPost(dispatchers)
    getAttractContent(Unit)

    val content by getAttractContent.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    val ads = content?.ads.orEmpty()
    val rotationSeconds = content?.rotationSeconds ?: 8

    return AttractUiState(
        ads = ads,
        currentIndex = currentIndex,
        rotationSeconds = rotationSeconds,
        eventSink = { event ->
            when (event) {
                AttractEvent.TouchToOrder -> navigator.goTo(IdentifyScreen(next = KioskOrderScreen))
                is AttractEvent.IndexChanged -> currentIndex = event.index
            }
        },
    )
}
