package com.jkjamies.sampleplatter.core.test

import com.jkjamies.sampleplatter.core.auth.AuthTokens
import com.jkjamies.sampleplatter.core.auth.RefreshTokenSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeRefreshTokenSource(
    var result: AuthTokens? = null,
    var error: Throwable? = null,
) : RefreshTokenSource {

    private val mutex = Mutex()
    private var count: Int = 0
    private var gate: CompletableDeferred<Unit>? = null

    val callCount: Int get() = count

    fun holdNextCallsUntilReleased() {
        gate = CompletableDeferred()
    }

    fun release() {
        gate?.complete(Unit)
        gate = null
    }

    override suspend fun refresh(refreshToken: String): AuthTokens? {
        mutex.withLock { count++ }
        gate?.await()
        error?.let { throw it }
        return result
    }
}
