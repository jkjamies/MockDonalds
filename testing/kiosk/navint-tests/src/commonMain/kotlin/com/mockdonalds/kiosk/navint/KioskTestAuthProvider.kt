package com.mockdonalds.kiosk.navint

import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.core.test.FakeAuthManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Provides a `FakeAuthManager` to the kiosk navint graph. Mirrors the consumer
 * `TestAuthProvider` — kiosk identify use cases want a real-shaped `AuthManager`
 * to call `login()` against, but tests don't want real auth network round-trips.
 */
@ContributesTo(AppScope::class)
interface KioskTestAuthProvider {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAuthManager(): AuthManager = FakeAuthManager()
}
