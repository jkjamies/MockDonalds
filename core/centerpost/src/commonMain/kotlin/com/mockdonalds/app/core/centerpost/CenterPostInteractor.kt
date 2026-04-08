package com.mockdonalds.app.core.centerpost

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

public abstract class CenterPostInteractor<in P, R> {

    private val loadingState = MutableStateFlow(State())

    @OptIn(FlowPreview::class)
    public val inProgress: Flow<Boolean> by lazy {
        loadingState
            .debounce {
                if (it.ambientCount > 0) { 5.seconds } else { 0.seconds }
            }
            .map { (it.userCount + it.ambientCount) > 0 }
            .distinctUntilChanged()
    }

    private fun addLoader(fromUser: Boolean) {
        loadingState.update {
            if (fromUser) {
                it.copy(userCount = it.userCount + 1)
            } else {
                it.copy(ambientCount = it.ambientCount + 1)
            }
        }
    }

    private fun removeLoader(fromUser: Boolean) {
        loadingState.update {
            if (fromUser) {
                it.copy(userCount = it.userCount - 1)
            } else {
                it.copy(ambientCount = it.ambientCount - 1)
            }
        }
    }

    public suspend operator fun invoke(
        params: P,
        timeout: Duration = DefaultTimeout,
        userInitiated: Boolean = params.isUserInitiated,
    ): CenterPostResult<R> = withLoader(userInitiated) {
        try {
            centerPostRunCatching {
                withTimeout(timeout) { doWork(params) }
            }
        } catch (e: TimeoutCancellationException) {
            CenterPostResult.Failure(CenterPostTimeoutException(timeout, e))
        }
    }

    private inline fun <T> withLoader(fromUser: Boolean, block: () -> T): T {
        addLoader(fromUser)
        try {
            return block()
        } finally {
            removeLoader(fromUser)
        }
    }

    private val P.isUserInitiated: Boolean
        get() = (this as? CenterPostUserInitiatedParams)?.isUserInitiated ?: true

    protected abstract suspend fun doWork(params: P): R

    public companion object {
        internal val DefaultTimeout: Duration = 5.minutes
    }

    private data class State(val userCount: Int = 0, val ambientCount: Int = 0)
}

public suspend operator fun <R> CenterPostInteractor<Unit, R>.invoke(
    timeout: Duration = CenterPostInteractor.DefaultTimeout,
): CenterPostResult<R> = invoke(Unit, timeout)
