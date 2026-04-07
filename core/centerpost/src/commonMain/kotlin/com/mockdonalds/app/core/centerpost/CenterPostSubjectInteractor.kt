package com.mockdonalds.app.core.centerpost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
public abstract class CenterPostSubjectInteractor<P : Any, T> {

    private val paramState = MutableSharedFlow<P>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    public val flow: Flow<T> = paramState
        .distinctUntilChanged()
        .flatMapLatest { createObservable(it) }
        .distinctUntilChanged()

    public operator fun invoke(params: P) {
        paramState.tryEmit(params)
    }

    protected abstract fun createObservable(params: P): Flow<T>
}

@Composable
public fun <T> CenterPostSubjectInteractor<Unit, T>.collectAsState(
    initial: T? = null,
): State<T?> {
    invoke(Unit)
    return flow.collectAsState(initial = initial)
}
