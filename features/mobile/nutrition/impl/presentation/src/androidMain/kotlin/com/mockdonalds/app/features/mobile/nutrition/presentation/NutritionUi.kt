package com.mockdonalds.app.features.mobile.nutrition.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.mockdonalds.app.core.presentation.webview.WebViewContent
import com.mockdonalds.app.core.presentation.webview.rememberWebViewState
import com.mockdonalds.app.features.mobile.nutrition.api.navigation.NutritionScreen
import com.mockdonalds.app.features.mobile.nutrition.api.ui.NutritionTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(NutritionScreen::class, AppScope::class)
@Composable
fun NutritionUi(state: NutritionUiState, modifier: Modifier = Modifier) {
    val webViewState = rememberWebViewState()
    BackHandler(enabled = webViewState.canGoBack) { webViewState.goBack() }

    Scaffold(
        modifier = modifier.testTag(NutritionTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text("Nutrition") },
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(NutritionEvent.BackClicked) },
                        modifier = Modifier.testTag(NutritionTestTags.BACK_BUTTON),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val url = state.url) {
            null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> WebViewContent(
                url = url,
                allowJs = true,
                state = webViewState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag(NutritionTestTags.WEBVIEW),
            )
        }
    }
}
