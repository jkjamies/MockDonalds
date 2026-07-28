package com.jkjamies.sampleplatter.core.remoteconfig.impl

import app.cash.turbine.test
import com.jkjamies.sampleplatter.core.remoteconfig.FeatureFlag
import com.jkjamies.sampleplatter.core.remoteconfig.RemoteConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class RemoteConfigProviderImplTest : BehaviorSpec({

    val testFlag = FeatureFlag(key = "test_flag", defaultValue = false)
    val testLongConfig = RemoteConfig.LongConfig(key = "test_long", default = 0L)

    Given("a provider with a remote source returning the default flag value") {
        val flagValues = MutableStateFlow(false)
        val source = TestRemoteConfigSource(flagState = flagValues)
        val provider = RemoteConfigProviderImpl(source)

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

    Given("a provider with a remote source returning an overridden flag value") {
        val flagValues = MutableStateFlow(true)
        val source = TestRemoteConfigSource(flagState = flagValues)
        val provider = RemoteConfigProviderImpl(source)

        When("checking isEnabled") {
            Then("it should return the remote value") {
                provider.isEnabled(testFlag) shouldBe true
            }
        }

        When("the remote value changes") {
            Then("observing should emit the updated value") {
                provider.observe(testFlag).test {
                    awaitItem() shouldBe true
                    flagValues.value = false
                    awaitItem() shouldBe false
                }
            }
        }
    }

    Given("a provider with a remote source returning a Long config value above the Double-precision boundary") {
        // 2^53 + 1 — a value that round-trips lossy through Double.
        val largeLong = 9_007_199_254_740_993L
        val configValues = MutableStateFlow(largeLong)
        val source = TestRemoteConfigSource(longState = configValues)
        val provider = RemoteConfigProviderImpl(source)

        When("getConfig is called") {
            Then("it should return the exact 64-bit value with no precision loss") {
                provider.getConfig(testLongConfig) shouldBe largeLong
            }
        }

        When("observeConfig is called") {
            Then("it should emit the exact 64-bit value") {
                provider.observeConfig(testLongConfig).test {
                    awaitItem() shouldBe largeLong
                }
            }
        }
    }
})

private class TestRemoteConfigSource(
    private val flagState: MutableStateFlow<Boolean> = MutableStateFlow(false),
    private val longState: MutableStateFlow<Long> = MutableStateFlow(0L),
) : RemoteConfigSource {
    override fun isEnabled(flag: FeatureFlag): Boolean = flagState.value
    override fun observe(flag: FeatureFlag): Flow<Boolean> = flagState

    @Suppress("UNCHECKED_CAST")
    override fun <T> getConfig(config: RemoteConfig<T>): T = when (config) {
        is RemoteConfig.LongConfig -> longState.value as T
        else -> config.default
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> observeConfig(config: RemoteConfig<T>): Flow<T> = when (config) {
        is RemoteConfig.LongConfig -> longState as Flow<T>
        else -> MutableStateFlow(config.default)
    }
}
