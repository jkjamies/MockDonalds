package com.jkjamies.sampleplatter.core.auth

interface RefreshTokenSource {
    suspend fun refresh(refreshToken: String): AuthTokens?
}
