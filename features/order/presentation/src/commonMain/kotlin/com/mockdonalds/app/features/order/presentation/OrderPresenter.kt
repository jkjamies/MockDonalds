package com.mockdonalds.app.features.order.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.collectAsState
import com.mockdonalds.app.core.centerpost.rememberCenterPost
import com.mockdonalds.app.features.order.api.domain.GetOrderContent
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
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
    dispatchers: CenterPostDispatchers,
): OrderUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by getOrderContent.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    val categories = content?.categories ?: emptyList()
    val effectiveSelectedId = selectedCategoryId ?: categories.firstOrNull()?.id

    return OrderUiState(
        categories = categories,
        selectedCategoryId = effectiveSelectedId,
        featuredItems = content?.featuredItems ?: emptyList(),
        cartSummary = content?.cartSummary,
        eventSink = { event ->
            when (event) {
                is OrderEvent.CategorySelected -> selectedCategoryId = event.id
                is OrderEvent.AddToOrder -> centerPost { }
                is OrderEvent.CartClicked -> centerPost { }
            }
        },
    )
}
