package com.mockdonalds.app.features.order.data.wiring

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.core.network.AuthMode
import com.mockdonalds.app.core.network.HttpClientFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@ContributesTo(AppScope::class)
interface SpoonacularHttpClientProvider {

    @Provides
    @SingleIn(AppScope::class)
    @SpoonacularHttpClient
    fun provideSpoonacularHttpClient(
        factory: HttpClientFactory,
        buildConfig: AppBuildConfig,
    ): HttpClient = factory.create {
        baseUrl = SPOONACULAR_BASE_URL
        authMode = AuthMode.NONE
        header(HEADER_X_API_KEY, buildConfig.spoonacularApiKey)
    }

    private companion object {
        // Third-party reference API; does not route through Akamai. See features/order/impl/data/AGENTS.md.
        const val SPOONACULAR_BASE_URL = "https://api.spoonacular.com/food"
        const val HEADER_X_API_KEY = "x-api-key"
    }
}
