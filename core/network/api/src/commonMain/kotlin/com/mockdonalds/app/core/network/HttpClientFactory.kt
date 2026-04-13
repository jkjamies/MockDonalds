package com.mockdonalds.app.core.network

import io.ktor.client.HttpClient

/**
 * Factory for creating per-feature [HttpClient] instances.
 *
 * Each client comes pre-configured with platform infrastructure (content negotiation,
 * app ID header, Akamai headers, market headers, logging). Features configure their
 * specific needs (base URL, timeouts, auth mode) via the [ClientConfig] DSL.
 */
interface HttpClientFactory {
    fun create(block: ClientConfig.() -> Unit = {}): HttpClient
}
