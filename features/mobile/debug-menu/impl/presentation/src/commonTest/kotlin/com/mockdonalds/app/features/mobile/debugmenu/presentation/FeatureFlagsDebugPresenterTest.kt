package com.mockdonalds.app.features.mobile.debugmenu.presentation

import com.mockdonalds.app.core.remoteconfig.FeatureFlag
import com.mockdonalds.app.core.remoteconfig.FeatureFlagDefinition
import com.mockdonalds.app.core.remoteconfig.FlagLifecycle
import com.mockdonalds.app.core.remoteconfig.test.FakeRemoteConfigProvider
import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.mobile.debugmenu.api.navigation.FeatureFlagsDebugScreen
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class FeatureFlagsDebugPresenterTest : BehaviorSpec({

    Given("a feature flags debug presenter") {
        val dispatchers = TestCenterPostDispatchers()

        When("no flags are registered") {
            Then("it should expose an empty row list") {
                val navigator = FakeNavigator(FeatureFlagsDebugScreen, FeatureFlagsDebugScreen)
                presenterTestOf(
                    presentFunction = {
                        FeatureFlagsDebugPresenter(
                            navigator = navigator,
                            dispatchers = dispatchers,
                            definitions = emptySet(),
                            remoteConfig = FakeRemoteConfigProvider(),
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.rows.isEmpty() shouldBe true
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("flags are registered") {
            Then("rows should reflect the definitions sorted by owner then key") {
                val navigator = FakeNavigator(FeatureFlagsDebugScreen, FeatureFlagsDebugScreen)
                val flagA = FeatureFlag(key = "order.new_checkout", defaultValue = false)
                val flagB = FeatureFlag(key = "account.new_profile", defaultValue = true)
                val provider = FakeRemoteConfigProvider()
                provider.setEnabled(flagA, enabled = true)
                presenterTestOf(
                    presentFunction = {
                        FeatureFlagsDebugPresenter(
                            navigator = navigator,
                            dispatchers = dispatchers,
                            definitions = setOf(
                                TestFlagDefinition(flagA, "new checkout", "order", FlagLifecycle.Experiment),
                                TestFlagDefinition(flagB, "new profile", "account", FlagLifecycle.KillSwitch),
                            ),
                            remoteConfig = provider,
                        )
                    },
                ) {
                    // rememberFlag seeds State with flag.defaultValue, then recomposes once the
                    // MutableStateFlow-backed observe(...) emits. Skip the initial-defaults frame
                    // and assert against the converged state.
                    awaitItem()
                    val state = awaitItem()
                    state.rows.map { it.key } shouldBe listOf("account.new_profile", "order.new_checkout")
                    state.rows.first { it.key == flagA.key }.enabled shouldBe true
                    state.rows.first { it.key == flagB.key }.enabled shouldBe true
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("the user taps back") {
            Then("it should pop the navigator") {
                val navigator = FakeNavigator(FeatureFlagsDebugScreen)
                presenterTestOf(
                    presentFunction = {
                        FeatureFlagsDebugPresenter(
                            navigator = navigator,
                            dispatchers = dispatchers,
                            definitions = emptySet(),
                            remoteConfig = FakeRemoteConfigProvider(),
                        )
                    },
                ) {
                    val state = awaitItem()
                    state.eventSink(FeatureFlagsDebugEvent.BackClicked)
                    navigator.awaitPop()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})

private data class TestFlagDefinition(
    override val flag: FeatureFlag,
    override val description: String,
    override val owner: String,
    override val lifecycle: FlagLifecycle,
) : FeatureFlagDefinition
