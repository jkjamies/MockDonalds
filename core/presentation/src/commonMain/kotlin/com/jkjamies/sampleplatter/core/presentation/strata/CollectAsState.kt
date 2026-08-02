package com.jkjamies.sampleplatter.core.presentation.strata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.jkjamies.sampleplatter.core.strata.StrataContentState
import com.jkjamies.sampleplatter.core.strata.StrataSubjectInteractor

/**
 * Collects the interactor with loading and failure states made explicit.
 *
 * Prefer this over [collectAsState] in presenters. The `State<T?>` returned by that overload
 * collapses "loading", "empty", and "failed" into a single `null`, so a screen cannot show a
 * spinner or an error — and an exception from the underlying flow reaches composition.
 *
 * Flatten the result into primitive `UiState` fields rather than putting
 * [StrataContentState] on the `UiState` itself — sealed interfaces do not bridge cleanly
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
public fun <T> StrataSubjectInteractor<Unit, T>.collectContentAsState(): State<StrataContentState<T>> {
    // Same seeding contract as collectAsState: invoke(Unit) once per interactor instance.
    LaunchedEffect(this) {
        invoke(Unit)
    }
    // Typed explicitly rather than passing `StrataContentState.Loading` inline: Compose's
    // `Flow<T>.collectAsState(initial: R)` is generic in both T and R, and `Loading` is a
    // `StrataContentState<Nothing>`, so pinning R here keeps inference off the critical path.
    val initial: StrataContentState<T> = StrataContentState.Loading
    return contentState.collectAsState(initial = initial)
}

@Composable
public fun <T> StrataSubjectInteractor<Unit, T>.collectAsState(
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
