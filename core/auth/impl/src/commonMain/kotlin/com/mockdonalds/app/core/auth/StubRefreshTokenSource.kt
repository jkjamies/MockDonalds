package com.mockdonalds.app.core.auth

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class StubRefreshTokenSource : RefreshTokenSource {
    override suspend fun refresh(refreshToken: String): AuthTokens? = null
}
