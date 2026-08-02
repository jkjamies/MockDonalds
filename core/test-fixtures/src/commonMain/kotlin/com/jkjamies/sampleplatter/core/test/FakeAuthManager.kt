package com.jkjamies.sampleplatter.core.test

import com.jkjamies.sampleplatter.core.auth.AuthManager
import com.jkjamies.sampleplatter.core.auth.AuthTokens

class FakeAuthManager(
    override var isAuthenticated: Boolean = false,
    var tokens: AuthTokens? = null,
    var refreshResult: AuthTokens? = null,
) : AuthManager {
    var refreshCallCount: Int = 0
        private set

    override fun login() {
        isAuthenticated = true
    }

    override fun logout() {
        isAuthenticated = false
        tokens = null
    }

    override suspend fun currentTokens(): AuthTokens? = tokens

    override suspend fun refresh(): AuthTokens? {
        refreshCallCount++
        tokens = refreshResult
        return refreshResult
    }
}
