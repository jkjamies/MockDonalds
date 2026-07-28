package com.jkjamies.sampleplatter.features.order.data.wiring

import com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfig
import com.jkjamies.sampleplatter.core.network.AuthMode
import com.jkjamies.sampleplatter.core.network.HttpClientFactory
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
    ): HttpClient {
        require(buildConfig.spoonacularApiKey.isNotBlank()) {
            "spoonacularApiKey must be set in local.properties for the order feature to function. " +
                "Sign up at https://spoonacular.com/food-api (free tier) and add " +
                "`spoonacularApiKey=YOUR_KEY` to local.properties at the repo root."
        }
        return factory.create {
            baseUrl = SPOONACULAR_BASE_URL
            authMode = AuthMode.NONE
            header(HEADER_X_API_KEY, buildConfig.spoonacularApiKey)
        }
    }

    private companion object {
        // Third-party reference API; does not route through Akamai. See features/order/impl/data/AGENTS.md.
        // Trailing `/` is required — Ktor's URL resolution (RFC 3986) strips the last segment
        // when appending a relative path against a base URL without trailing slash, so
        // "https://api.spoonacular.com/food" + "menuItems/search" would resolve to
        // "https://api.spoonacular.com/menuItems/search" (404). With the trailing slash,
        // resolution preserves the /food prefix correctly.
        const val SPOONACULAR_BASE_URL = "https://api.spoonacular.com/food/"
        const val HEADER_X_API_KEY = "x-api-key"
    }
}
