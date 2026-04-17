package com.mockdonalds.app.features.recents.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.core.theme.adaptiveBottomBarPadding
import com.mockdonalds.app.features.recents.api.domain.RecentItem
import com.mockdonalds.app.features.recents.api.navigation.RecentsScreen
import com.mockdonalds.app.features.recents.api.ui.RecentsTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(RecentsScreen::class, AppScope::class)
@Composable
fun RecentsUi(state: RecentsUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.testTag(RecentsTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text("Recents") },
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(RecentsEvent.OnBackTapped) },
                        modifier = Modifier.testTag(RecentsTestTags.BACK_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = adaptiveBottomBarPadding())
        ) {
            when (state) {
                is RecentsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RecentsUiState.Empty -> {
                    RecentsEmptyUi(modifier = Modifier.align(Alignment.Center))
                }
                is RecentsUiState.Success -> {
                    RecentsSuccessUi(items = state.items, eventSink = state.eventSink)
                }
            }
        }
    }
}

@Composable
fun RecentsEmptyUi(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.testTag(RecentsTestTags.EMPTY),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(MockDimens.SpacingLg))
        Text(
            text = "No recent activity",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(MockDimens.SpacingSm))
        Text(
            text = "Your recent orders and items will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecentsSuccessUi(items: List<RecentItem>, eventSink: (RecentsEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(RecentsTestTags.LIST),
        contentPadding = PaddingValues(MockDimens.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd)
    ) {
        items(items) { item ->
            RecentItemCard(item = item, onClick = { eventSink(RecentsEvent.OnItemTapped(item.id)) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentItemCard(item: RecentItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${RecentsTestTags.ITEM}-${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(MockDimens.RadiusMd)
    ) {
        Row(
            modifier = Modifier.padding(MockDimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(MockDimens.RadiusSm)),
                color = MaterialTheme.colorScheme.surface
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(MockDimens.SpacingMd))
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.relativeTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
