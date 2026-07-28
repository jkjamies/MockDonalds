package com.jkjamies.sampleplatter.features.debugmenu.presentation

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.jkjamies.sampleplatter.core.theme.PlatterDimens
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.FeatureFlagsDebugScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.ui.FeatureFlagsDebugTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(FeatureFlagsDebugScreen::class, AppScope::class)
@Composable
fun FeatureFlagsDebugUi(state: FeatureFlagsDebugUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.testTag(FeatureFlagsDebugTestTags.ROOT),
        topBar = {
            TopAppBar(
                title = { Text("Feature Flags") },
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(FeatureFlagsDebugEvent.BackClicked) },
                        modifier = Modifier.testTag(FeatureFlagsDebugTestTags.BACK_BUTTON),
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
        if (state.rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No feature flags registered",
                    modifier = Modifier.testTag(FeatureFlagsDebugTestTags.EMPTY_STATE),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag(FeatureFlagsDebugTestTags.FLAG_LIST),
                contentPadding = PaddingValues(PlatterDimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(PlatterDimens.SpacingSm),
            ) {
                items(state.rows, key = { it.key }) { row ->
                    FeatureFlagCard(row)
                }
            }
        }
    }
}

@Composable
private fun FeatureFlagCard(row: FeatureFlagRow) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${FeatureFlagsDebugTestTags.FLAG_ROW}-${row.key}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(PlatterDimens.RadiusMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(PlatterDimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = row.key, style = MaterialTheme.typography.titleSmall)
                if (row.description.isNotEmpty()) {
                    Text(
                        text = row.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = buildString {
                        if (row.owner.isNotEmpty()) append(row.owner).append(" • ")
                        append(row.lifecycle.name)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = row.enabled,
                onCheckedChange = null,
            )
        }
    }
}
