package com.jkjamies.sampleplatter.core.logger.test

import com.jkjamies.sampleplatter.core.logger.LoggerInitializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface LoggerTestBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideLoggerInitializer(): LoggerInitializer = NoOpLoggerInitializer()
}
