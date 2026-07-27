package com.mockdonalds.app.core.centerpost

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

    private val retrySignal = MutableStateFlow(0)

    /**
     * Params, restarted on [retry].
     *
     * [distinctUntilChanged] is what makes re-`invoke`ing with an unchanged value cheap, and it
     * is also why a failed stream cannot be restarted by calling `invoke(sameParams)` again.
     * Combining a monotonically increasing retry counter re-emits the current params without
     * weakening that dedup for ordinary invocations.
     */
    private val restartableParams: Flow<P> =
        combine(paramState.distinctUntilChanged(), retrySignal) { params, _ -> params }

    /**
     * The raw value stream.
     *
     * Emits only successful values: there is no loading signal, and an exception thrown by
     * [createObservable] propagates to the collector — which, for a presenter collecting via
     * Compose, means it surfaces in composition. Prefer [contentState] in presenters; this
     * remains for callers that compose flows together and handle failure themselves.
     */
    public val flow: Flow<T> = restartableParams
        .flatMapLatest { createObservable(it) }
        .distinctUntilChanged()

    /**
     * The same stream with loading and failure modelled explicitly.
     *
     * Emits [CenterPostContentState.Loading] before the first value for a given set of params,
     * [CenterPostContentState.Content] per value, and [CenterPostContentState.Error] if the
     * underlying flow throws — instead of letting the exception escape into the collector.
     *
     * [CenterPostContentState.Error] is terminal for the current params. Re-invoking with the
     * same params will *not* restart the stream — params are deduped — so a "Retry" affordance
     * must call [retry], not `invoke`.
     */
    public val contentState: Flow<CenterPostContentState<T>> = restartableParams
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

    /**
     * Restarts the stream for the params currently in flight.
     *
     * This is the retry path for [CenterPostContentState.Error]. No-op until [invoke] has been
     * called at least once — there are no params to restart with.
     */
    public fun retry() {
        retrySignal.value += 1
    }

    protected abstract fun createObservable(params: P): Flow<T>
}
