package com.mockdonalds.app.features.kiosk.identify.presentation

import com.mockdonalds.app.core.test.TestCenterPostDispatchers
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyResult
import com.mockdonalds.app.features.kiosk.identify.api.navigation.IdentifyScreen
import com.mockdonalds.app.features.kiosk.identify.test.FakeContinueAsGuest
import com.mockdonalds.app.features.kiosk.identify.test.FakeGetIdentifyContent
import com.mockdonalds.app.features.kiosk.identify.test.FakeIdentifyByPhoneNumber
import com.mockdonalds.app.features.kiosk.identify.test.FakeIdentifyByQrCode
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class IdentifyPresenterTest : BehaviorSpec({

    Given("a kiosk identify presenter with default fakes") {
        val getContent = FakeGetIdentifyContent()
        val byPhone = FakeIdentifyByPhoneNumber()
        val byQr = FakeIdentifyByQrCode()
        val asGuest = FakeContinueAsGuest()
        val dispatchers = TestCenterPostDispatchers()
        val screen = IdentifyScreen(next = null)
        val navigator = FakeNavigator(screen)

        When("a digit is pressed from the initial state") {
            Then("the presenter exposes the typed digit and content defaults") {
                presenterTestOf(
                    presentFunction = {
                        IdentifyPresenter(
                            screen = screen,
                            navigator = navigator,
                            getIdentifyContent = getContent,
                            identifyByPhoneNumber = byPhone,
                            identifyByQrCode = byQr,
                            continueAsGuest = asGuest,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.eventSink(IdentifyEvent.DigitPressed('5'))
                    var state = awaitItem()
                    while (state.phoneInput.isEmpty()) state = awaitItem()
                    state.phoneInput shouldBe "5"
                    state.isSubmitting shouldBe false
                    state.errorMessage shouldBe null
                    state.skipEnabled shouldBe true
                    state.qrScannerEnabled shouldBe true
                    state.countryDialCode shouldBe FakeGetIdentifyContent.DEFAULT.countryDialCode
                }
            }
        }

        When("a too-short phone is submitted") {
            Then("the presenter rejects with an error message") {
                presenterTestOf(
                    presentFunction = {
                        IdentifyPresenter(
                            screen = screen,
                            navigator = navigator,
                            getIdentifyContent = getContent,
                            identifyByPhoneNumber = byPhone,
                            identifyByQrCode = byQr,
                            continueAsGuest = asGuest,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    listOf('1', '2', '3').forEach { initial.eventSink(IdentifyEvent.DigitPressed(it)) }
                    var state = awaitItem()
                    while (state.phoneInput.length < 3) state = awaitItem()
                    state.phoneInput shouldBe "123"
                    state.eventSink(IdentifyEvent.SubmitPhonePressed)
                    val errored = awaitItem()
                    errored.errorMessage shouldBe "Enter at least 7 digits"
                }
            }
        }

        When("the skip button is pressed") {
            Then("the presenter sets isSubmitting=true while ContinueAsGuest is in flight") {
                asGuest.nextResult = IdentifyResult.GuestSession
                presenterTestOf(
                    presentFunction = {
                        IdentifyPresenter(
                            screen = screen,
                            navigator = navigator,
                            getIdentifyContent = getContent,
                            identifyByPhoneNumber = byPhone,
                            identifyByQrCode = byQr,
                            continueAsGuest = asGuest,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.eventSink(IdentifyEvent.SkipPressed)
                    var state = awaitItem()
                    while (!state.isSubmitting) state = awaitItem()
                    state.isSubmitting shouldBe true
                }
            }
        }
    }
})
