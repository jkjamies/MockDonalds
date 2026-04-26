package com.mockdonalds.app.core.auth

interface RefreshTokenSource {
    suspend fun refresh(refreshToken: String): AuthTokens?
}
