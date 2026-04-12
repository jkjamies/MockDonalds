package com.mockdonalds.app.core.featureflag.impl

import app.cash.turbine.test
import com.mockdonalds.app.core.featureflag.FeatureFlag
import com.mockdonalds.app.core.featureflag.FeatureFlagProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ObserveFeatureFlagImplTest : BehaviorSpec({

    val testFlag = FeatureFlag(key = "test_flag", defaultValue = false)

    Given("an ObserveFeatureFlagImpl with a provider") {
        val flagValues = MutableStateFlow(false)

        val provider = object : FeatureFlagProvider {
            override fun isEnabled(flag: FeatureFlag): Boolean = flagValues.value
            override fun observe(flag: FeatureFlag): Flow<Boolean> = flagValues
        }

        val interactor = ObserveFeatureFlagImpl(provider)

        When("the interactor is invoked and flow is collected") {
            Then("it should emit the provider value") {
                interactor(testFlag)
                interactor.flow.test {
                    awaitItem() shouldBe false
                }
            }
        }

        When("the provider value changes") {
            Then("the interactor flow should emit the updated value") {
                interactor(testFlag)
                interactor.flow.test {
                    awaitItem() shouldBe false
                    flagValues.value = true
                    awaitItem() shouldBe true
                }
            }
        }
    }
})
