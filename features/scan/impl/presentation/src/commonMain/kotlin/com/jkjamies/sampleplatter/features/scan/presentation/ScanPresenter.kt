package com.jkjamies.sampleplatter.features.scan.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.centerpost.CenterPostDispatchers
import com.jkjamies.sampleplatter.core.presentation.centerpost.collectAsState
import com.jkjamies.sampleplatter.core.presentation.centerpost.rememberCenterPost
import com.jkjamies.sampleplatter.features.scan.api.domain.GetScanContent
import com.jkjamies.sampleplatter.features.scan.api.navigation.ScanScreen
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
