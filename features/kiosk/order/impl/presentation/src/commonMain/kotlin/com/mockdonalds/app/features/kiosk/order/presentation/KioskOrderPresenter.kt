package com.mockdonalds.app.features.kiosk.order.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.presentation.centerpost.collectAsState
import com.mockdonalds.app.core.presentation.centerpost.rememberCenterPost
import com.mockdonalds.app.features.kiosk.attract.api.navigation.AttractScreen
import com.mockdonalds.app.features.kiosk.order.api.navigation.KioskOrderScreen
import com.mockdonalds.app.features.shared.menu.api.domain.GetOrderContent
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(KioskOrderScreen::class, AppScope::class)
@Inject
@Composable
fun KioskOrderPresenter(
    navigator: Navigator,
    getOrderContent: GetOrderContent,
    dispatchers: CenterPostDispatchers,
): KioskOrderUiState {
    rememberCenterPost(dispatchers)
    getOrderContent(Unit)

    val content by getOrderContent.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    val categories = content?.categories.orEmpty()
    val effectiveSelectedId = selectedCategoryId ?: categories.firstOrNull()?.id
    val itemsForSelected = effectiveSelectedId
        ?.let { content?.itemsByCategory?.get(it) }
        .orEmpty()

    return KioskOrderUiState(
        categories = categories,
        selectedCategoryId = effectiveSelectedId,
        itemsForSelectedCategory = itemsForSelected,
        cartSummary = content?.cartSummary,
        eventSink = { event ->
            when (event) {
                is KioskOrderEvent.CategorySelected -> selectedCategoryId = event.id
                is KioskOrderEvent.ItemTapped -> Unit // Phase: future KioskItemDetailScreen
                KioskOrderEvent.ScanOfferPressed -> Unit // Phase: future ScanOfferScreen overlay
                KioskOrderEvent.CartPressed -> Unit // Phase: future KioskCartReviewScreen
                KioskOrderEvent.PayPressed -> Unit // Phase: future KioskPaymentScreen
                KioskOrderEvent.BackPressed,
                KioskOrderEvent.CancelOrderPressed,
                -> navigator.resetRoot(AttractScreen)
            }
        },
    )
}
