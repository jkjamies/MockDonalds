package com.mockdonalds.app.core.centerpost

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
public abstract class CenterPostSubjectInteractor<P : Any, T> {

    private val paramState = MutableSharedFlow<P>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * The raw value stream.
     *
     * Emits only successful values: there is no loading signal, and an exception thrown by
     * [createObservable] propagates to the collector — which, for a presenter collecting via
     * Compose, means it surfaces in composition. Prefer [contentState] in presenters; this
     * remains for callers that compose flows together and handle failure themselves.
     */
    public val flow: Flow<T> = paramState
        .distinctUntilChanged()
        .flatMapLatest { createObservable(it) }
        .distinctUntilChanged()

    /**
     * The same stream with loading and failure modelled explicitly.
     *
     * Emits [CenterPostContentState.Loading] before the first value for a given set of params,
     * [CenterPostContentState.Content] per value, and [CenterPostContentState.Error] if the
     * underlying flow throws — instead of letting the exception escape into the collector.
     *
     * Re-invoking with the same params does not restart a failed stream ([distinctUntilChanged]
     * on params); pass different params, or expose an explicit retry that re-invokes.
     */
    public val contentState: Flow<CenterPostContentState<T>> = paramState
        .distinctUntilChanged()
        .flatMapLatest { params ->
            createObservable(params)
                .map<T, CenterPostContentState<T>> { CenterPostContentState.Content(it) }
                .onStart { emit(CenterPostContentState.Loading) }
                .catch { throwable ->
                    // Cancellation is control flow, not failure — never swallow it, or a
                    // cancelled collector would be reported to the UI as an error state.
                    if (throwable is CancellationException) throw throwable
                    emit(CenterPostContentState.Error(throwable.asCenterPostException()))
                }
        }
        .distinctUntilChanged()

    public operator fun invoke(params: P) {
        paramState.tryEmit(params)
    }

    protected abstract fun createObservable(params: P): Flow<T>
}
