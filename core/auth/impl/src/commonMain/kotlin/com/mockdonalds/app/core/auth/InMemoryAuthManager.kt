package com.mockdonalds.app.core.auth

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class InMemoryAuthManager(
    private val refreshSource: RefreshTokenSource,
) : AuthManager {

    private val mutex = Mutex()
    private var tokens: AuthTokens? = null
    private var inflight: CompletableDeferred<AuthTokens?>? = null

    override val isAuthenticated: Boolean
        get() = tokens != null

    override fun login() {
        tokens = DevTokens
    }

    override fun logout() {
        tokens = null
    }

    override suspend fun currentTokens(): AuthTokens? = tokens

    override suspend fun refresh(): AuthTokens? {
        val leader = CompletableDeferred<AuthTokens?>()
        val (deferred, isLeader) = mutex.withLock {
            val existing = inflight
            if (existing != null) {
                existing to false
            } else {
                inflight = leader
                leader to true
            }
        }
        if (!isLeader) return deferred.await()

        val newTokens = runCatching {
            val refreshToken = tokens?.refreshToken ?: return@runCatching null
            refreshSource.refresh(refreshToken)
        }
        mutex.withLock {
            tokens = newTokens.getOrNull()
            inflight = null
        }
        newTokens.fold(
            onSuccess = { leader.complete(it) },
            onFailure = { leader.completeExceptionally(it) },
        )
        return leader.await()
    }

    private companion object {
        val DevTokens = AuthTokens(accessToken = "dev-access", refreshToken = "dev-refresh")
    }
}
