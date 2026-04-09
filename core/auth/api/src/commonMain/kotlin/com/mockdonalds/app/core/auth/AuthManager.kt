package com.mockdonalds.app.core.auth

interface AuthManager {
    val isAuthenticated: Boolean
    fun login()
    fun logout()
}
