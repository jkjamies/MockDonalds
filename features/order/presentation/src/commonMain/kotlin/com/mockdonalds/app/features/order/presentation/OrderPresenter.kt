package com.mockdonalds.app.features.order.presentation

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Inject

@CircuitInject(OrderScreen::class, AppScope::class)
@Inject
class OrderPresenter(
    private val navigator: Navigator,
) : Presenter<OrderUiState> {

    @Composable
    override fun present(): OrderUiState {
        return OrderUiState(
            eventSink = {},
        )
    }
}
