package com.mockdonalds.app.core.featureflag.impl

import app.cash.turbine.test
import com.mockdonalds.app.core.featureflag.FeatureFlag
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FeatureFlagProviderImplTest : BehaviorSpec({

    val testFlag = FeatureFlag(key = "test_flag", defaultValue = false)

    Given("a provider with a remote source returning the default value") {
        val remoteValues = MutableStateFlow(false)

        val remoteSource = object : RemoteFeatureFlagSource {
            override fun isEnabled(flag: FeatureFlag): Boolean = remoteValues.value
            override fun observe(flag: FeatureFlag): Flow<Boolean> = remoteValues
        }

        val provider = FeatureFlagProviderImpl(remoteSource)

        When("checking isEnabled") {
            Then("it should return the default value") {
                provider.isEnabled(testFlag) shouldBe false
            }
        }

        When("observing the flag") {
            Then("it should emit the default value") {
                provider.observe(testFlag).test {
                    awaitItem() shouldBe false
                }
            }
        }
    }

    Given("a provider with a remote source returning an overridden value") {
        val remoteValues = MutableStateFlow(true)

        val remoteSource = object : RemoteFeatureFlagSource {
            override fun isEnabled(flag: FeatureFlag): Boolean = remoteValues.value
            override fun observe(flag: FeatureFlag): Flow<Boolean> = remoteValues
        }

        val provider = FeatureFlagProviderImpl(remoteSource)

        When("checking isEnabled") {
            Then("it should return the remote value") {
                provider.isEnabled(testFlag) shouldBe true
            }
        }

        When("the remote value changes") {
            Then("observing should emit the updated value") {
                provider.observe(testFlag).test {
                    awaitItem() shouldBe true
                    remoteValues.value = false
                    awaitItem() shouldBe false
                }
            }
        }
    }
})
