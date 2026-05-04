package com.mockdonalds.app.navint

import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.core.test.FakeAuthManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface TestAuthProvider {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAuthManager(): AuthManager = FakeAuthManager()
}
