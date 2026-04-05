package com.mockdonalds.app.features.order.presentation

import androidx.compose.runtime.Composable
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope

@CircuitInject(OrderScreen::class, AppScope::class)
@Composable
fun OrderPresenter(
    navigator: Navigator,
): OrderUiState {
    return OrderUiState(
        eventSink = {},
    )
}
