package com.jkjamies.sampleplatter.core.auth

interface AuthManager {
    val isAuthenticated: Boolean
    fun login()
    fun logout()

    suspend fun currentTokens(): AuthTokens?
    suspend fun refresh(): AuthTokens?
}
