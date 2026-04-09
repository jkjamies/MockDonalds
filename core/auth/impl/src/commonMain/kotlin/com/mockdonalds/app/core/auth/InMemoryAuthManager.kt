package com.mockdonalds.app.core.auth

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class InMemoryAuthManager @Inject constructor() : AuthManager {
    override var isAuthenticated: Boolean = false
        private set

    override fun login() {
        isAuthenticated = true
    }

    override fun logout() {
        isAuthenticated = false
    }
}
