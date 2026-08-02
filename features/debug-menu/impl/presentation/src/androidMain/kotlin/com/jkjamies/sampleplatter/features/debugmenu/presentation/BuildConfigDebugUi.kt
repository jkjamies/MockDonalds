package com.jkjamies.sampleplatter.features.debugmenu.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.jkjamies.sampleplatter.core.buildconfig.BuildConfigField
import com.jkjamies.sampleplatter.core.theme.PlatterDimens
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.ui.BuildConfigDebugTestTags
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
        val grouped = state.fields.groupBy { it.group }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag(BuildConfigDebugTestTags.FIELD_LIST),
            contentPadding = PaddingValues(PlatterDimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(PlatterDimens.SpacingSm),
        ) {
            BuildConfigField.Group.entries.forEach { group ->
                val fields = grouped[group].orEmpty()
                if (fields.isEmpty()) return@forEach

                item(key = "header-${group.name}") { GroupHeader(group) }
                items(fields, key = { "${group.name}-${it.name}" }) { field ->
                    FieldRow(field)
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(group: BuildConfigField.Group) {
    Text(
        text = group.name,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PlatterDimens.SpacingMd, bottom = PlatterDimens.SpacingXs)
            .testTag("${BuildConfigDebugTestTags.GROUP_HEADER}-${group.name}"),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun FieldRow(field: BuildConfigField) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${BuildConfigDebugTestTags.FIELD_ROW}-${field.name}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        shape = RoundedCornerShape(PlatterDimens.RadiusMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(PlatterDimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = field.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = field.value.ifEmpty { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "read-only",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
