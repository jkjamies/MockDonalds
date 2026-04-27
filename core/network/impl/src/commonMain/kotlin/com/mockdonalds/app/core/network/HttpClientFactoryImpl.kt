package com.mockdonalds.app.core.network

import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.core.auth.AuthTokens
import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class HttpClientFactoryImpl(
    private val appBuildConfig: AppBuildConfig,
    private val json: Json,
    private val authManager: AuthManager,
    private val sensorDataProvider: SensorDataProvider,
    private val baseClient: HttpClient,
) : HttpClientFactory {

    private val userAgent: String =
        "${appBuildConfig.appName}/${appBuildConfig.env}-${appBuildConfig.market} " +
            "(${appBuildConfig.buildType})"

    private val isProd: Boolean get() = appBuildConfig.env == "prod"

    override fun create(block: ClientConfig.() -> Unit): HttpClient {
        val config = ClientConfig().apply(block)

        val sensorHeaderPlugin = createClientPlugin("AkamaiSensorDataHeader") {
            onRequest { request, _ ->
                val data = sensorDataProvider.currentSensorData()
                if (data.isNotEmpty()) request.headers[HEADER_AKAMAI_SENSOR] = data
            }
        }

        return baseClient.config {
            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds
                connectTimeoutMillis = config.connectTimeout.inWholeMilliseconds
                socketTimeoutMillis = config.socketTimeout.inWholeMilliseconds
            }

            install(Logging) {
                level = if (isProd) LogLevel.NONE else LogLevel.HEADERS
            }

            install(UserAgent) { agent = userAgent }

            install(HttpCookies)

            installRequestRetry()

            install(sensorHeaderPlugin)

            if (config.authMode == AuthMode.BEARER) {
                installBearerAuth(config.baseUrl)
            }

            defaultRequest {
                config.baseUrl?.let { url(it) }
                header(HEADER_APP_ID, appBuildConfig.appId)
                header(HEADER_MARKET, appBuildConfig.market)
                if (!isProd) {
                    AKAMAI_DEBUG_PRAGMAS.forEach { header(HEADER_PRAGMA, it) }
                }
                config.headers.forEach { (name, value) -> header(name, value) }
            }

            config.ktorBlock?.invoke(this)
        }
    }

    private fun HttpClientConfig<*>.installRequestRetry() {
        install(HttpRequestRetry) {
            retryIf(maxRetries = MAX_RETRIES) { request, response ->
                val retryableStatus = response.status.value in RETRYABLE_SERVER_ERRORS ||
                    response.status.value == STATUS_TOO_MANY_REQUESTS
                val retrySafe = request.method in IDEMPOTENT_METHODS ||
                    request.headers.contains(HEADER_IDEMPOTENCY_KEY)
                retryableStatus && retrySafe
            }
            retryOnExceptionIf(maxRetries = MAX_RETRIES) { request, _ ->
                request.method in IDEMPOTENT_METHODS ||
                    request.headers.contains(HEADER_IDEMPOTENCY_KEY)
            }
            exponentialDelay(respectRetryAfterHeader = true)
        }
    }

    private fun HttpClientConfig<*>.installBearerAuth(baseUrl: String?) {
        val authorized = Url(
            checkNotNull(baseUrl) {
                "AuthMode.BEARER requires baseUrl to be set so the bearer token " +
                    "can be scoped to a single host."
            },
        )
        install(Auth) {
            bearer {
                loadTokens { authManager.currentTokens()?.toBearer() }
                refreshTokens {
                    val responded = response.call.request.url
                    val matches = responded.host.equals(authorized.host, ignoreCase = true) &&
                        responded.protocol == authorized.protocol
                    if (!matches) return@refreshTokens null
                    authManager.refresh()?.toBearer()
                }
                sendWithoutRequest { request ->
                    request.url.host.equals(authorized.host, ignoreCase = true) &&
                        request.url.protocol == authorized.protocol
                }
            }
        }
    }

    private fun AuthTokens.toBearer(): BearerTokens =
        BearerTokens(accessToken = accessToken, refreshToken = refreshToken)

    private companion object {
        const val HEADER_APP_ID = "X-App-Id"
        const val HEADER_MARKET = "X-Market"
        const val HEADER_AKAMAI_SENSOR = "X-acf-sensor-data"
        const val HEADER_PRAGMA = "Pragma"
        const val HEADER_IDEMPOTENCY_KEY = "Idempotency-Key"
        const val MAX_RETRIES = 3
        const val STATUS_TOO_MANY_REQUESTS = 429

        // Only methods that are idempotent under RFC 9110 §9.2.2 may be retried automatically.
        // Writes (POST, PATCH) opt in via Idempotency-Key so the backend can dedupe.
        val IDEMPOTENT_METHODS = setOf(
            HttpMethod.Get,
            HttpMethod.Head,
            HttpMethod.Options,
            HttpMethod.Put,
            HttpMethod.Delete,
        )

        // Narrowed from blanket 5xx: 501 and 505 are non-transient, 500 is ambiguous.
        // Akamai/origin failures we actually expect to retry are 502/503/504.
        val RETRYABLE_SERVER_ERRORS = setOf(502, 503, 504)

        val AKAMAI_DEBUG_PRAGMAS = listOf(
            "akamai-x-cache-on",
            "akamai-x-cache-remote-on",
            "akamai-x-get-true-cache-key",
            "akamai-x-get-cache-key",
        )
    }
}
