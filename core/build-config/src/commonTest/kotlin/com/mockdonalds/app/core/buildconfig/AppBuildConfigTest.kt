package com.mockdonalds.app.core.buildconfig

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.string.shouldMatch

class AppBuildConfigTest : BehaviorSpec({

    Given("the active AppBuildConfig for this build") {
        val config: AppBuildConfig = AppBuildConfigImpl()

        When("reading each field") {
            Then("appName is MockDonalds") {
                config.appName shouldBe "MockDonalds"
            }

            Then("appId is a non-empty identifier") {
                config.appId shouldNotBe ""
            }

            Then("market is a non-empty lowercase identifier") {
                config.market shouldNotBe ""
                config.market shouldMatch Regex("^[a-z]{2}$")
            }

            Then("env is one of the known environments") {
                config.env shouldNotBe ""
                listOf("dev", "stg", "prod") shouldContain config.env
            }

            Then("baseUrl is an https URL") {
                config.baseUrl shouldNotBe ""
                config.baseUrl shouldMatch Regex("^https://.+")
            }

            Then("cdnUrl is an https URL") {
                config.cdnUrl shouldNotBe ""
                config.cdnUrl shouldMatch Regex("^https://.+")
            }

            Then("menuBaseUrl is an https URL") {
                config.menuBaseUrl shouldNotBe ""
                config.menuBaseUrl shouldMatch Regex("^https://.+")
            }

            Then("orderBaseUrl is an https URL") {
                config.orderBaseUrl shouldNotBe ""
                config.orderBaseUrl shouldMatch Regex("^https://.+")
            }

            Then("accountBaseUrl is an https URL") {
                config.accountBaseUrl shouldNotBe ""
                config.accountBaseUrl shouldMatch Regex("^https://.+")
            }

            Then("rewardsBaseUrl is an https URL") {
                config.rewardsBaseUrl shouldNotBe ""
                config.rewardsBaseUrl shouldMatch Regex("^https://.+")
            }

            Then("storeBaseUrl is an https URL") {
                config.storeBaseUrl shouldNotBe ""
                config.storeBaseUrl shouldMatch Regex("^https://.+")
            }

            Then("locale is a BCP-47-shaped tag") {
                config.locale shouldMatch Regex("^[a-z]{2}-[A-Z]{2}$")
            }

            Then("currency is an ISO-4217 code") {
                config.currency shouldMatch Regex("^[A-Z]{3}$")
            }
        }
    }
})
