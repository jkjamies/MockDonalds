package com.mockdonalds.app.core.network

import io.ktor.client.HttpClientConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * DSL for configuring a per-feature [io.ktor.client.HttpClient].
 *
 * Common settings are typed properties. For advanced Ktor plugin installation,
 * use [ktorConfig] as an escape hatch.
 */
class ClientConfig {

    /**
     * Base URL for all requests made by this client. Required when [authMode] is [AuthMode.BEARER]
     * — the bearer token is host-pinned to this URL's host and protocol so credentials cannot
     * leak to absolute-URL requests pointing at other hosts.
     */
    var baseUrl: String? = null

    /** Auth mode for this client. Defaults to [AuthMode.BEARER]. */
    var authMode: AuthMode = AuthMode.BEARER

    /** Request timeout. Overrides the factory default. */
    var requestTimeout: Duration = 15.seconds

    /** Connection timeout. Overrides the factory default. */
    var connectTimeout: Duration = 5.seconds

    /** Socket (read/write) timeout. Overrides the factory default. */
    var socketTimeout: Duration = 10.seconds

    /** Additional headers applied to every request from this client. */
    val headers: MutableMap<String, String> = mutableMapOf()

    /** Escape hatch for advanced Ktor configuration (custom plugins, etc.). */
    var ktorBlock: (HttpClientConfig<*>.() -> Unit)? = null
        private set

    /** Add a custom header to every request. */
    fun header(name: String, value: String) {
        headers[name] = value
    }

    /**
     * Escape hatch for direct Ktor [HttpClientConfig] access.
     * Use for installing custom plugins or configuring options not exposed by this DSL.
     */
    fun ktorConfig(block: HttpClientConfig<*>.() -> Unit) {
        ktorBlock = block
    }
}

/** Auth mode for a client created by [HttpClientFactory]. */
enum class AuthMode {
    /**
     * Bearer token auth with automatic refresh on 401. Token is scoped to the client's
     * [ClientConfig.baseUrl] host + protocol; cross-host requests through the same client get no
     * `Authorization` header. Requires [ClientConfig.baseUrl] to be non-null.
     */
    BEARER,

    /** No auth headers. Use for public/unauthenticated endpoints (login, bootstrap). */
    NONE,
}
