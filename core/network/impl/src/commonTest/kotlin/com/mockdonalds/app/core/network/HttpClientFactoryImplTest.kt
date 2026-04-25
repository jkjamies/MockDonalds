package com.mockdonalds.app.core.network

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.core.test.FakeAuthManager
import com.mockdonalds.app.core.test.FakeSensorDataProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.time.Duration.Companion.seconds

class HttpClientFactoryImplTest : BehaviorSpec({

    fun buildConfig(env: String = "dev") = object : AppBuildConfig {
        override val appName = "MockDonalds"
        override val appId = "us-mockdonalds-mobile-$env"
        override val market = "us"
        override val env = env
        override val buildType = "debug"
        override val baseUrl = "https://$env-api.mockdonalds.com"
        override val cdnUrl = "https://$env-cdn.mockdonalds.com"
        override val menuBaseUrl = "https://$env-menu-api.mockdonalds.com"
        override val orderBaseUrl = "https://$env-order-api.mockdonalds.com"
        override val accountBaseUrl = "https://$env-account-api.mockdonalds.com"
        override val rewardsBaseUrl = "https://$env-rewards-api.mockdonalds.com"
        override val storeBaseUrl = "https://$env-stores-api.mockdonalds.com"
        override val locale = "en-US"
        override val currency = "USD"
    }

    val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun factoryWith(
        engine: MockEngine,
        env: String = "dev",
        sensor: FakeSensorDataProvider = FakeSensorDataProvider(),
    ): HttpClientFactoryImpl = HttpClientFactoryImpl(
        appBuildConfig = buildConfig(env),
        json = json,
        authManager = FakeAuthManager(),
        sensorDataProvider = sensor,
        baseClient = HttpClient(engine),
    )

    Given("a HttpClientFactoryImpl") {

        When("creating multiple clients") {
            val engine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
            val factory = factoryWith(engine)
            val a = factory.create { baseUrl = "https://a.mockdonalds.com" }
            val b = factory.create { baseUrl = "https://b.mockdonalds.com" }

            Then("each is a distinct HttpClient sharing the same engine") {
                a shouldNotBe b
                a.engine shouldBe b.engine
                a.close()
                b.close()
            }
        }

        When("the ktorConfig escape hatch is used") {
            var escapeHatchCalled = false
            val engine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
            factoryWith(engine).create {
                baseUrl = "https://order-api.mockdonalds.com"
                ktorConfig { escapeHatchCalled = true }
            }

            Then("the block is invoked") {
                escapeHatchCalled shouldBe true
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
                val client = factoryWith(engine, sensor = sensor).create {
                    baseUrl = "https://menu-api.mockdonalds.com"
                    authMode = AuthMode.NONE
                }

                client.get("/menu")
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
                val client = factoryWith(engine).create {
                    baseUrl = "https://menu-api.mockdonalds.com"
                    authMode = AuthMode.NONE
                }

                client.get("/menu")
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
                val client = factoryWith(engine, env = "dev").create {
                    baseUrl = "https://menu-api.mockdonalds.com"
                    authMode = AuthMode.NONE
                }

                client.get("/menu")
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
                val client = factoryWith(engine, env = "prod").create {
                    baseUrl = "https://menu-api.mockdonalds.com"
                    authMode = AuthMode.NONE
                }

                client.get("/menu")
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
                val client = factoryWith(engine, env = "stage").create {
                    baseUrl = "https://menu-api.mockdonalds.com"
                    authMode = AuthMode.NONE
                }

                client.get("/menu")
                appId shouldBe "us-mockdonalds-mobile-stage"
                market shouldBe "us"
                userAgent!! shouldContain "MockDonalds/stage-us (debug)"
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
                val client = factoryWith(engine).create {
                    baseUrl = "https://menu-api.mockdonalds.com"
                    authMode = AuthMode.NONE
                }

                client.get("/menu")
                client.get("/menu")

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
                val client = factoryWith(engine).create {
                    baseUrl = "https://menu-api.mockdonalds.com"
                    authMode = AuthMode.NONE
                }

                val response: HttpResponse = client.get("/menu")
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
                val client = factoryWith(engine).create {
                    baseUrl = "https://menu-api.mockdonalds.com"
                    authMode = AuthMode.NONE
                    requestTimeout = 30.seconds
                }

                val response: HttpResponse = client.get("/menu")
                response.status shouldBe HttpStatusCode.OK
                calls shouldBe 2
            }
        }
    }
})
