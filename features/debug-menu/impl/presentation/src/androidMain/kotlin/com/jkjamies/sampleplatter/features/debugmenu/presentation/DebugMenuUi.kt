package com.jkjamies.sampleplatter.features.debugmenu.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.jkjamies.sampleplatter.core.theme.PlatterDimens
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.DebugMenuScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.ui.DebugMenuTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DebugMenuScreen::class, AppScope::class)
@Composable
fun DebugMenuUi(state: DebugMenuUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.testTag(DebugMenuTestTags.ROOT),
        topBar = {
            TopAppBar(
                title = { Text("Debug Menu") },
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(DebugMenuEvent.BackClicked) },
                        modifier = Modifier.testTag(DebugMenuTestTags.BACK_BUTTON),
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag(DebugMenuTestTags.ENTRY_LIST),
                contentPadding = PaddingValues(PlatterDimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(PlatterDimens.SpacingMd),
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    DebugEntryCard(entry = entry) {
                        state.eventSink(DebugMenuEvent.EntryClicked(entry.id))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugEntryCard(entry: DebugMenuEntry, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${DebugMenuTestTags.ENTRY_ITEM}-${entry.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(PlatterDimens.RadiusMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(PlatterDimens.SpacingLg),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(text = ">", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
