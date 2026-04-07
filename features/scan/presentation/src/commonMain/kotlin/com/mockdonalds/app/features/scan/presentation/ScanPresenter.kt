package com.mockdonalds.app.features.scan.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.collectAsState
import com.mockdonalds.app.core.centerpost.rememberCenterPost
import com.mockdonalds.app.features.scan.api.domain.GetScanContent
import com.mockdonalds.app.features.scan.api.navigation.ScanScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(ScanScreen::class, AppScope::class)
@Inject
@Composable
fun ScanPresenter(
    navigator: Navigator,
    getScanContent: GetScanContent,
    dispatchers: CenterPostDispatchers,
): ScanUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by getScanContent.collectAsState()

    return ScanUiState(
        memberInfo = content?.memberInfo,
        rewardsProgress = content?.rewardsProgress,
        eventSink = { event ->
            when (event) {
                is ScanEvent.PayNowClicked -> centerPost { }
                is ScanEvent.ViewOffersClicked -> centerPost { }
            }
        },
    )
}
