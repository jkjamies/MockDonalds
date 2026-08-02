package com.jkjamies.sampleplatter.features.order.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.presentation.strata.collectAsState
import com.jkjamies.sampleplatter.core.presentation.strata.rememberStrata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
import com.jkjamies.sampleplatter.features.order.api.domain.GetOrderContent
import com.jkjamies.sampleplatter.features.order.api.navigation.CategoryDetailScreen
import com.jkjamies.sampleplatter.features.order.api.navigation.OrderScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(OrderScreen::class, AppScope::class)
@Inject
@Composable
fun OrderPresenter(
    navigator: Navigator,
    getOrderContent: GetOrderContent,
    dispatchers: StrataDispatchers,
): OrderUiState {
    val strata = rememberStrata(dispatchers)
    val content by getOrderContent.collectAsState()

    return OrderUiState(
        categoryPreviews = content?.categoryPreviews ?: emptyList(),
        cartSummary = content?.cartSummary,
        eventSink = { event ->
            when (event) {
                is OrderEvent.CategoryTapped -> navigator.goTo(CategoryDetailScreen(event.id))
                is OrderEvent.CartClicked -> strata { }
            }
        },
    )
}
