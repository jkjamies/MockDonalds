package com.jkjamies.sampleplatter.core.network

import com.jkjamies.sampleplatter.core.auth.AuthTokens
import com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfig
import com.jkjamies.sampleplatter.core.test.FakeAuthManager
import com.jkjamies.sampleplatter.core.test.FakeSensorDataProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.time.Duration.Companion.seconds

class HttpClientFactoryImplTest : BehaviorSpec({

    fun buildConfig(env: String = "dev") = object : AppBuildConfig {
        override val appName = "SamplePlatter"
        override val appId = "us-sampleplatter-mobile-$env"
        override val market = "us"
        override val env = env
        override val buildType = "debug"
        override val baseUrl = "https://$env-api.sampleplatter.com"
        override val cdnUrl = "https://$env-cdn.sampleplatter.com"
        override val menuBaseUrl = "https://$env-menu-api.sampleplatter.com"
        override val orderBaseUrl = "https://$env-order-api.sampleplatter.com"
        override val accountBaseUrl = "https://$env-account-api.sampleplatter.com"
        override val rewardsBaseUrl = "https://$env-rewards-api.sampleplatter.com"
        override val storeBaseUrl = "https://$env-stores-api.sampleplatter.com"
        override val nutritionUrl = "https://$env-nutrition.sampleplatter.com"
        override val locale = "en-US"
        override val currency = "USD"
        override val spoonacularApiKey = ""
    }

    val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun factoryWith(
        engine: MockEngine,
        env: String = "dev",
        sensor: FakeSensorDataProvider = FakeSensorDataProvider(),
        authManager: FakeAuthManager = FakeAuthManager(),
    ): HttpClientFactoryImpl = HttpClientFactoryImpl(
        appBuildConfig = buildConfig(env),
        json = json,
        authManager = authManager,
        sensorDataProvider = sensor,
        baseClient = HttpClient(engine),
    )

    Given("a HttpClientFactoryImpl") {

        When("creating multiple clients") {
            val engine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
            val factory = factoryWith(engine)
            val a = factory.create { baseUrl = "https://a.sampleplatter.com" }
            val b = factory.create { baseUrl = "https://b.sampleplatter.com" }

            Then("each is a distinct HttpClient sharing the same engine") {
                try {
                    a shouldNotBe b
                    a.engine shouldBe b.engine
                } finally {
                    a.close()
                    b.close()
                }
            }
        }

        When("the ktorConfig escape hatch is used") {
            Then("the block is invoked") {
                var escapeHatchCalled = false
                val engine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
                factoryWith(engine).create {
                    baseUrl = "https://order-api.sampleplatter.com"
                    ktorConfig { escapeHatchCalled = true }
                }.use {
                    escapeHatchCalled shouldBe true
                }
            }
        }
    }

    Given("Akamai headers") {

        When("a request is sent with non-empty sensor data") {
            Then("the X-acf-sensor-data header carries the token") {
                var captured: String? = null
                val engine = MockEngine { request ->
                    captured = request.headers["X-acf-sensor-data"]
                    respond(content = "", status = HttpStatusCode.OK)
                }
                val sensor = FakeSensorDataProvider(data = "sensor-token-xyz")
                factoryWith(engine, sensor = sensor).create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                }
                captured shouldBe "sensor-token-xyz"
            }
        }

        When("sensor data is empty") {
            Then("no X-acf-sensor-data header is sent") {
                var captured: String? = "not-touched"
                val engine = MockEngine { request ->
                    captured = request.headers["X-acf-sensor-data"]
                    respond(content = "", status = HttpStatusCode.OK)
                }
                factoryWith(engine).create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                }
                captured shouldBe null
            }
        }

        When("a request is sent in non-prod") {
            Then("Akamai debug Pragma headers are included") {
                var pragmas: List<String> = emptyList()
                val engine = MockEngine { request ->
                    pragmas = request.headers.getAll("Pragma").orEmpty()
                    respond(content = "", status = HttpStatusCode.OK)
                }
                factoryWith(engine, env = "dev").create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                }
                pragmas shouldContainAll listOf(
                    "akamai-x-cache-on",
                    "akamai-x-get-true-cache-key",
                )
            }
        }

        When("a request is sent in prod") {
            Then("Akamai debug Pragma headers are omitted") {
                var pragmas: List<String> = emptyList()
                val engine = MockEngine { request ->
                    pragmas = request.headers.getAll("Pragma").orEmpty()
                    respond(content = "", status = HttpStatusCode.OK)
                }
                factoryWith(engine, env = "prod").create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                }
                pragmas shouldNotContain "akamai-x-cache-on"
            }
        }

        When("a request is sent") {
            Then("X-App-Id, X-Market, and User-Agent are populated") {
                var appId: String? = null
                var market: String? = null
                var userAgent: String? = null
                val engine = MockEngine { request ->
                    appId = request.headers["X-App-Id"]
                    market = request.headers["X-Market"]
                    userAgent = request.headers[HttpHeaders.UserAgent]
                    respond(content = "", status = HttpStatusCode.OK)
                }
                factoryWith(engine, env = "stage").create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                }
                appId shouldBe "us-sampleplatter-mobile-stage"
                market shouldBe "us"
                userAgent!! shouldContain "SamplePlatter/stage-us (debug)"
            }
        }
    }

    Given("cookies") {

        When("the server sets a cookie on one response") {
            Then("the next request on the same client replays that cookie") {
                val observed = mutableListOf<String?>()
                var first = true
                val engine = MockEngine { request ->
                    observed += request.headers[HttpHeaders.Cookie]
                    if (first) {
                        first = false
                        respond(
                            content = "",
                            status = HttpStatusCode.OK,
                            headers = headersOf(
                                HttpHeaders.SetCookie,
                                "ak_bmsc=session-123; Path=/",
                            ),
                        )
                    } else {
                        respond(content = "", status = HttpStatusCode.OK)
                    }
                }
                factoryWith(engine).create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                    client.get("/menu")
                }

                observed[0] shouldBe null
                observed[1]!! shouldContain "ak_bmsc=session-123"
            }
        }
    }

    Given("retry behavior") {

        When("the server returns 503 twice then 200") {
            Then("the request retries and eventually succeeds") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    if (calls < 3) {
                        respond(content = "", status = HttpStatusCode.ServiceUnavailable)
                    } else {
                        respond(content = "ok", status = HttpStatusCode.OK)
                    }
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                }
                response.status shouldBe HttpStatusCode.OK
                calls shouldBe 3
            }
        }

        When("the server returns 429 once then 200") {
            Then("the request is retried") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    if (calls == 1) {
                        respond(
                            content = "",
                            status = HttpStatusCode.TooManyRequests,
                            headers = headersOf(HttpHeaders.RetryAfter, "0"),
                        )
                    } else {
                        respond(content = "ok", status = HttpStatusCode.OK)
                    }
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                    requestTimeout = 30.seconds
                }.use { client ->
                    client.get("/menu")
                }
                response.status shouldBe HttpStatusCode.OK
                calls shouldBe 2
            }
        }

        When("a POST receives 503") {
            Then("the request is NOT retried (writes are not idempotent)") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    respond(
                        content = "",
                        status = HttpStatusCode.ServiceUnavailable,
                        headers = headersOf(HttpHeaders.RetryAfter, "0"),
                    )
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://order-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.post("/orders")
                }
                response.status shouldBe HttpStatusCode.ServiceUnavailable
                calls shouldBe 1
            }
        }

        When("a POST receives 429") {
            Then("the request is NOT retried (writes are not idempotent)") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    respond(
                        content = "",
                        status = HttpStatusCode.TooManyRequests,
                        headers = headersOf(HttpHeaders.RetryAfter, "0"),
                    )
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://order-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.post("/orders")
                }
                response.status shouldBe HttpStatusCode.TooManyRequests
                calls shouldBe 1
            }
        }

        When("a POST carries an Idempotency-Key and receives 503") {
            Then("the request is retried because the backend can dedupe") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    if (calls < 2) {
                        respond(
                            content = "",
                            status = HttpStatusCode.ServiceUnavailable,
                            headers = headersOf(HttpHeaders.RetryAfter, "0"),
                        )
                    } else {
                        respond(content = "ok", status = HttpStatusCode.OK)
                    }
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://order-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                    requestTimeout = 30.seconds
                }.use { client ->
                    client.post("/orders") {
                        header("Idempotency-Key", "order-abc-123")
                    }
                }
                response.status shouldBe HttpStatusCode.OK
                calls shouldBe 2
            }
        }

        When("a PUT receives 503") {
            Then("the request is retried (PUT is idempotent)") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    if (calls < 2) {
                        respond(
                            content = "",
                            status = HttpStatusCode.ServiceUnavailable,
                            headers = headersOf(HttpHeaders.RetryAfter, "0"),
                        )
                    } else {
                        respond(content = "ok", status = HttpStatusCode.OK)
                    }
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://account-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                    requestTimeout = 30.seconds
                }.use { client ->
                    client.put("/profile")
                }
                response.status shouldBe HttpStatusCode.OK
                calls shouldBe 2
            }
        }

        When("a DELETE receives 503") {
            Then("the request is retried (DELETE is idempotent)") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    if (calls < 2) {
                        respond(
                            content = "",
                            status = HttpStatusCode.ServiceUnavailable,
                            headers = headersOf(HttpHeaders.RetryAfter, "0"),
                        )
                    } else {
                        respond(content = "", status = HttpStatusCode.NoContent)
                    }
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://account-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                    requestTimeout = 30.seconds
                }.use { client ->
                    client.delete("/profile")
                }
                response.status shouldBe HttpStatusCode.NoContent
                calls shouldBe 2
            }
        }

        When("the server returns 501 Not Implemented") {
            Then("the request is NOT retried (501 is not transient)") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    respond(content = "", status = HttpStatusCode.NotImplemented)
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                }
                response.status shouldBe HttpStatusCode.NotImplemented
                calls shouldBe 1
            }
        }

        When("the server returns 500") {
            Then("the request is NOT retried (500 is ambiguous and may indicate a write succeeded)") {
                var calls = 0
                val engine = MockEngine {
                    calls++
                    respond(content = "", status = HttpStatusCode.InternalServerError)
                }
                val response: HttpResponse = factoryWith(engine).create {
                    baseUrl = "https://menu-api.sampleplatter.com"
                    authMode = AuthMode.NONE
                }.use { client ->
                    client.get("/menu")
                }
                response.status shouldBe HttpStatusCode.InternalServerError
                calls shouldBe 1
            }
        }
    }

    Given("bearer token host scoping") {

        val devTokens = AuthTokens(accessToken = "live-access", refreshToken = "live-refresh")

        When("a BEARER client makes a request to its own baseUrl host") {
            Then("the Authorization header is attached") {
                var auth: String? = null
                val engine = MockEngine { request ->
                    auth = request.headers[HttpHeaders.Authorization]
                    respond(content = "", status = HttpStatusCode.OK)
                }
                factoryWith(
                    engine = engine,
                    authManager = FakeAuthManager(tokens = devTokens, isAuthenticated = true),
                ).create {
                    baseUrl = "https://order-api.sampleplatter.com"
                    authMode = AuthMode.BEARER
                }.use { client ->
                    client.get("/orders")
                }
                auth shouldBe "Bearer live-access"
            }
        }

        When("a BEARER client makes a request to a different absolute-URL host") {
            Then("the Authorization header is NOT attached (token does not leak)") {
                var auth: String? = "not-touched"
                val engine = MockEngine { request ->
                    auth = request.headers[HttpHeaders.Authorization]
                    respond(content = "", status = HttpStatusCode.OK)
                }
                factoryWith(
                    engine = engine,
                    authManager = FakeAuthManager(tokens = devTokens, isAuthenticated = true),
                ).create {
                    baseUrl = "https://order-api.sampleplatter.com"
                    authMode = AuthMode.BEARER
                }.use { client ->
                    client.get("https://attacker.example.com/steal")
                }
                auth shouldBe null
            }
        }

        When("a BEARER client is configured without a baseUrl") {
            Then("creation fails fast (BEARER requires a host to scope the token)") {
                val engine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
                val factory = factoryWith(engine)

                shouldThrow<IllegalStateException> {
                    factory.create { authMode = AuthMode.BEARER }
                }
            }
        }
    }

    Given("bearer auth flow") {

        val expiredTokens = AuthTokens(accessToken = "expired-access", refreshToken = "old-refresh")
        val refreshedTokens = AuthTokens(accessToken = "new-access", refreshToken = "new-refresh")

        When("the matching-host server returns 401 with a Bearer challenge") {
            Then("the client refreshes exactly once and retries with the new token") {
                val observedAuth = mutableListOf<String?>()
                var calls = 0
                val engine = MockEngine { request ->
                    calls++
                    observedAuth += request.headers[HttpHeaders.Authorization]
                    if (calls == 1) {
                        respond(
                            content = "",
                            status = HttpStatusCode.Unauthorized,
                            headers = headersOf(
                                HttpHeaders.WWWAuthenticate,
                                "Bearer realm=\"api\"",
                            ),
                        )
                    } else {
                        respond(content = "ok", status = HttpStatusCode.OK)
                    }
                }
                val authManager = FakeAuthManager(
                    tokens = expiredTokens,
                    refreshResult = refreshedTokens,
                    isAuthenticated = true,
                )

                val response: HttpResponse = factoryWith(
                    engine = engine,
                    authManager = authManager,
                ).create {
                    baseUrl = "https://order-api.sampleplatter.com"
                    authMode = AuthMode.BEARER
                }.use { client ->
                    client.get("/orders")
                }

                response.status shouldBe HttpStatusCode.OK
                calls shouldBe 2
                authManager.refreshCallCount shouldBe 1
                observedAuth[0] shouldBe "Bearer expired-access"
                observedAuth[1] shouldBe "Bearer new-access"
            }
        }
    }
})
