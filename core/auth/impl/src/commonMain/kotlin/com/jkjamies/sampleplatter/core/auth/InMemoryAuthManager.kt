package com.jkjamies.sampleplatter.core.auth

import com.jkjamies.sampleplatter.core.logger.featureLogger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val log = featureLogger("Auth")

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
        log.i { "User logged in (dev tokens issued)" }
    }

    override fun logout() {
        tokens = null
        log.i { "User logged out" }
    }

    override suspend fun currentTokens(): AuthTokens? = tokens

    override suspend fun refresh(): AuthTokens? {
        val leader = CompletableDeferred<AuthTokens?>()
        val (deferred, isLeader) = mutex.withLock {
            val existing = inflight
            if (existing != null) {
                log.v { "Refresh already inflight — joining as follower" }
                existing to false
            } else {
                inflight = leader
                leader to true
            }
        }
        if (!isLeader) return deferred.await()

        log.d { "Refreshing tokens" }
        val newTokens = runCatching {
            val refreshToken = tokens?.refreshToken ?: return@runCatching null
            refreshSource.refresh(refreshToken)
        }
        mutex.withLock {
            tokens = newTokens.getOrNull()
            inflight = null
        }
        newTokens.fold(
            onSuccess = { fresh ->
                if (fresh == null) {
                    log.w { "Refresh returned no tokens (stub source or missing refresh token)" }
                } else {
                    log.i { "Tokens refreshed" }
                }
                leader.complete(fresh)
            },
            onFailure = { e ->
                log.e(e) { "Refresh failed" }
                leader.completeExceptionally(e)
            },
        )
        return leader.await()
    }

    private companion object {
        val DevTokens = AuthTokens(accessToken = "dev-access", refreshToken = "dev-refresh")
    }
}
