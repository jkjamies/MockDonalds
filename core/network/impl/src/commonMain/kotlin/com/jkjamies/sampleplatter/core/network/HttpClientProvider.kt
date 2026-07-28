package com.jkjamies.sampleplatter.core.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@ContributesTo(AppScope::class)
interface HttpClientProvider {
    @Provides
    @SingleIn(AppScope::class)
    fun provideBaseHttpClient(): HttpClient = HttpClient()
}
