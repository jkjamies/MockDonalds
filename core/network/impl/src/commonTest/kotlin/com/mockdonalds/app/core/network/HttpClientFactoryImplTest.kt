package com.mockdonalds.app.core.network

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.time.Duration.Companion.seconds

class HttpClientFactoryImplTest : BehaviorSpec({

    val appBuildConfig = object : AppBuildConfig {
        override val appName = "MockDonalds"
        override val appId = "us-mockdonalds-mobile-dev"
        override val market = "us"
        override val env = "dev"
        override val baseUrl = "https://dev-api.mockdonalds.com"
        override val cdnUrl = "https://dev-cdn.mockdonalds.com"
        override val menuBaseUrl = "https://dev-menu-api.mockdonalds.com"
        override val orderBaseUrl = "https://dev-order-api.mockdonalds.com"
        override val accountBaseUrl = "https://dev-account-api.mockdonalds.com"
        override val rewardsBaseUrl = "https://dev-rewards-api.mockdonalds.com"
        override val storeBaseUrl = "https://dev-stores-api.mockdonalds.com"
        override val locale = "en-US"
        override val currency = "USD"
    }

    val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val factory = HttpClientFactoryImpl(appBuildConfig, json)

    Given("a HttpClientFactoryImpl") {

        When("creating a client with default config") {
            val client = factory.create()

            Then("client is created successfully") {
                client shouldNotBe null
                client.close()
            }
        }

        When("creating a client with custom config") {
            val client = factory.create {
                baseUrl = "https://menu-api.mockdonalds.com"
                requestTimeout = 30.seconds
                connectTimeout = 10.seconds
                header("X-Custom", "test-value")
            }

            Then("client is created successfully") {
                client shouldNotBe null
                client.close()
            }
        }

        When("creating a client with no auth") {
            val client = factory.create {
                authMode = AuthMode.NONE
                baseUrl = "https://login-api.mockdonalds.com"
            }

            Then("client is created successfully") {
                client shouldNotBe null
                client.close()
            }
        }

        When("creating a client with ktorConfig escape hatch") {
            var escapeHatchCalled = false
            val client = factory.create {
                baseUrl = "https://order-api.mockdonalds.com"
                ktorConfig {
                    escapeHatchCalled = true
                }
            }

            Then("the escape hatch block is invoked") {
                escapeHatchCalled shouldBe true
                client.close()
            }
        }
    }
})
