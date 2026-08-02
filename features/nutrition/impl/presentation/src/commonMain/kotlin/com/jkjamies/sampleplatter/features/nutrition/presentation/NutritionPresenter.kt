package com.jkjamies.sampleplatter.features.nutrition.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.jkjamies.sampleplatter.core.presentation.strata.collectAsState
import com.jkjamies.sampleplatter.core.presentation.strata.rememberStrata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
import com.jkjamies.sampleplatter.features.nutrition.api.domain.GetNutritionContent
import com.jkjamies.sampleplatter.features.nutrition.api.navigation.NutritionScreen
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
    dispatchers: StrataDispatchers,
): NutritionUiState {
    rememberStrata(dispatchers)
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
