package com.mockdonalds.app.features.mobile.nutrition.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.presentation.centerpost.collectAsState
import com.mockdonalds.app.core.presentation.centerpost.rememberCenterPost
import com.mockdonalds.app.features.mobile.nutrition.api.domain.GetNutritionContent
import com.mockdonalds.app.features.mobile.nutrition.api.navigation.NutritionScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(NutritionScreen::class, AppScope::class)
@Inject
@Composable
fun NutritionPresenter(
    navigator: Navigator,
    getNutritionContent: GetNutritionContent,
    dispatchers: CenterPostDispatchers,
): NutritionUiState {
    rememberCenterPost(dispatchers)
    getNutritionContent(Unit)

    val content by getNutritionContent.collectAsState()

    return NutritionUiState(
        url = content?.url,
        eventSink = { event ->
            when (event) {
                NutritionEvent.BackClicked -> navigator.pop()
            }
        },
    )
}
