package com.jkjamies.sampleplatter.navint

import com.jkjamies.sampleplatter.core.auth.AuthManager
import com.jkjamies.sampleplatter.core.test.FakeAuthManager
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
