package com.jkjamies.sampleplatter.features.order.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.centerpost.CenterPostDispatchers
import com.jkjamies.sampleplatter.core.presentation.centerpost.collectAsState
import com.jkjamies.sampleplatter.core.presentation.centerpost.rememberCenterPost
import com.jkjamies.sampleplatter.features.order.api.domain.CategoryDetailContent
import com.jkjamies.sampleplatter.features.order.api.domain.GetCategoryDetailContent
import com.jkjamies.sampleplatter.features.order.api.domain.GetOrderContent
import com.jkjamies.sampleplatter.features.order.api.navigation.CategoryDetailScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(CategoryDetailScreen::class, AppScope::class)
@Inject
@Composable
fun CategoryDetailPresenter(
    screen: CategoryDetailScreen,
    navigator: Navigator,
    getCategoryDetailContent: GetCategoryDetailContent,
    getOrderContent: GetOrderContent,
    dispatchers: CenterPostDispatchers,
): CategoryDetailUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val detail: CategoryDetailContent? by getCategoryDetailContent.flow
        .collectAsState(initial = null)
    val orderContent by getOrderContent.collectAsState()

    LaunchedEffect(screen.categoryId) {
        getCategoryDetailContent(screen.categoryId)
    }

    return CategoryDetailUiState(
        categoryId = screen.categoryId,
        categoryName = detail?.categoryName.orEmpty(),
        items = detail?.items ?: emptyList(),
        cartSummary = orderContent?.cartSummary,
        eventSink = { event ->
            when (event) {
                is CategoryDetailEvent.BackPressed -> navigator.pop()
                is CategoryDetailEvent.AddToOrder -> centerPost { }
                is CategoryDetailEvent.CartClicked -> centerPost { }
            }
        },
    )
}
