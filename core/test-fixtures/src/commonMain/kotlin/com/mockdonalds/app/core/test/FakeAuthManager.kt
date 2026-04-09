package com.mockdonalds.app.core.test

import com.mockdonalds.app.core.auth.AuthManager

class FakeAuthManager(
    override var isAuthenticated: Boolean = false,
) : AuthManager {
    override fun login() {
        isAuthenticated = true
    }

    override fun logout() {
        isAuthenticated = false
    }
}
