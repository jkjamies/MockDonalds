package com.mockdonalds.app.core.presentation.centerpost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.mockdonalds.app.core.centerpost.CenterPostContentState
import com.mockdonalds.app.core.centerpost.CenterPostSubjectInteractor

/**
 * Collects the interactor with loading and failure states made explicit.
 *
 * Prefer this over [collectAsState] in presenters. The `State<T?>` returned by that overload
 * collapses "loading", "empty", and "failed" into a single `null`, so a screen cannot show a
 * spinner or an error — and an exception from the underlying flow reaches composition.
 *
 * Flatten the result into primitive `UiState` fields rather than putting
 * [CenterPostContentState] on the `UiState` itself — sealed interfaces do not bridge cleanly
 * to Swift:
 *
 * ```kotlin
 * val content by getOrderContent.collectContentAsState()
 * return OrderUiState(
 *     categoryPreviews = content.dataOrNull?.categoryPreviews.orEmpty(),
 *     isLoading = content.isLoading,
 *     errorMessage = content.errorOrNull?.message,
 *     eventSink = { ... },
 * )
 * ```
 */
@Composable
public fun <T> CenterPostSubjectInteractor<Unit, T>.collectContentAsState(): State<CenterPostContentState<T>> {
    // Same seeding contract as collectAsState: invoke(Unit) once per interactor instance.
    LaunchedEffect(this) {
        invoke(Unit)
    }
    return contentState.collectAsState(initial = CenterPostContentState.Loading)
}

@Composable
public fun <T> CenterPostSubjectInteractor<Unit, T>.collectAsState(
    initial: T? = null,
): State<T?> {
    // LaunchedEffect keyed on the interactor: invoke(Unit) runs once per instance
    // (not per recomposition), seeding paramState. flow.collectAsState then collects
    // continuously from the live data stream — reactivity comes from the underlying
    // createObservable, not from re-invoking.
    LaunchedEffect(this) {
        invoke(Unit)
    }
    return flow.collectAsState(initial = initial)
}
