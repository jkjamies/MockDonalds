package com.mockdonalds.app.features.debugmenu.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.mockdonalds.app.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.mockdonalds.app.features.debugmenu.api.ui.BuildConfigDebugTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(BuildConfigDebugScreen::class, AppScope::class)
@Composable
fun BuildConfigDebugUi(state: BuildConfigDebugUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.testTag(BuildConfigDebugTestTags.ROOT),
        topBar = {
            TopAppBar(
                title = { Text("Build Config") },
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(BuildConfigDebugEvent.BackClicked) },
                        modifier = Modifier.testTag(BuildConfigDebugTestTags.BACK_BUTTON),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Build Config details will be listed here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
