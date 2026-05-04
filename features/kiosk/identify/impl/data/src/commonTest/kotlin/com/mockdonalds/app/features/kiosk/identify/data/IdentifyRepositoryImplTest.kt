package com.mockdonalds.app.features.kiosk.identify.data

import app.cash.turbine.test
import com.mockdonalds.app.core.buildconfig.test.FakeAppBuildConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class IdentifyRepositoryImplTest : BehaviorSpec({

    Given("the kiosk identify repository implementation with a US dial code") {
        val appBuildConfig = FakeAppBuildConfig().apply { phoneCountryDialCode = "+1" }
        val repository = IdentifyRepositoryImpl(appBuildConfig)

        When("getting identify content") {
            Then("it should expose all three identification methods enabled with the configured dial code") {
                repository.getIdentifyContent().test {
                    val content = awaitItem()
                    content.skipEnabled shouldBe true
                    content.phoneEntryEnabled shouldBe true
                    content.qrScannerEnabled shouldBe true
                    content.countryDialCode shouldBe "+1"
                    awaitComplete()
                }
            }
        }
    }

    Given("the repository configured for the DE market") {
        val appBuildConfig = FakeAppBuildConfig().apply { phoneCountryDialCode = "+49" }
        val repository = IdentifyRepositoryImpl(appBuildConfig)

        When("getting identify content") {
            Then("the dial code should reflect the DE override") {
                repository.getIdentifyContent().test {
                    awaitItem().countryDialCode shouldBe "+49"
                    awaitComplete()
                }
            }
        }
    }
})
