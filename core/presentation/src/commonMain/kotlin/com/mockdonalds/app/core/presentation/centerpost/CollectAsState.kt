package com.mockdonalds.app.core.presentation.centerpost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.mockdonalds.app.core.centerpost.CenterPostSubjectInteractor

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
