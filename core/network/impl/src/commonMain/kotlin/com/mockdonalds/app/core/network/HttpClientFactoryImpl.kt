package com.mockdonalds.app.core.network

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@ContributesBinding(AppScope::class)
@Inject
class HttpClientFactoryImpl(
    private val appBuildConfig: AppBuildConfig,
    private val json: Json,
) : HttpClientFactory {

    override fun create(block: ClientConfig.() -> Unit): HttpClient {
        val config = ClientConfig().apply(block)

        return HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds
                connectTimeoutMillis = config.connectTimeout.inWholeMilliseconds
                socketTimeoutMillis = config.socketTimeout.inWholeMilliseconds
            }

            install(Logging) {
                level = if (appBuildConfig.env == "prod") LogLevel.NONE else LogLevel.HEADERS
            }

            defaultRequest {
                config.baseUrl?.let { url(it) }
                header(HEADER_APP_ID, appBuildConfig.appId)
                header(HEADER_MARKET, appBuildConfig.market)
                config.headers.forEach { (name, value) -> header(name, value) }
            }

            // Feature escape hatch — custom plugins, overrides, etc.
            config.ktorBlock?.invoke(this)
        }
    }

    private companion object {
        const val HEADER_APP_ID = "X-App-Id"
        const val HEADER_MARKET = "X-Market"
    }
}
