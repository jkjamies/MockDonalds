package com.jkjamies.sampleplatter.features.recents.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.centerpost.CenterPostDispatchers
import com.jkjamies.sampleplatter.core.presentation.centerpost.collectAsState
import com.jkjamies.sampleplatter.core.presentation.centerpost.rememberCenterPost
import com.jkjamies.sampleplatter.features.recents.api.domain.GetRecentsContent
import com.jkjamies.sampleplatter.features.recents.api.navigation.RecentsScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(RecentsScreen::class, AppScope::class)
@Inject
@Composable
fun RecentsPresenter(
    navigator: Navigator,
    getRecentsContent: GetRecentsContent,
    dispatchers: CenterPostDispatchers,
): RecentsUiState {
    rememberCenterPost(dispatchers)
    getRecentsContent(Unit)

    val content by getRecentsContent.collectAsState()

    val eventSink: (RecentsEvent) -> Unit = { event ->
        when (event) {
            is RecentsEvent.OnItemTapped -> {}
            RecentsEvent.OnBackTapped -> navigator.pop()
        }
    }

    val currentContent = content
    return when {
        currentContent == null -> RecentsUiState.Loading(eventSink = eventSink)
        currentContent.items.isEmpty() -> RecentsUiState.Empty(eventSink = eventSink)
        else -> RecentsUiState.Success(
            items = currentContent.items,
            eventSink = eventSink,
        )
    }
}
