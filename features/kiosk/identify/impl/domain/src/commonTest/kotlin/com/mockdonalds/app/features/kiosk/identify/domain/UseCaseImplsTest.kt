package com.mockdonalds.app.features.kiosk.identify.domain

import app.cash.turbine.test
import com.mockdonalds.app.core.centerpost.CenterPostResult
import com.mockdonalds.app.core.test.FakeAuthManager
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyContent
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetIdentifyContentImplTest : BehaviorSpec({

    Given("a GetIdentifyContent backed by a fake repository") {
        val initial = IdentifyContent(
            skipEnabled = true,
            phoneEntryEnabled = true,
            qrScannerEnabled = true,
            countryDialCode = "+1",
        )
        val source = MutableStateFlow(initial)
        val repository = object : IdentifyRepository {
            override fun getIdentifyContent(): Flow<IdentifyContent> = source
        }
        val interactor = GetIdentifyContentImpl(repository)

        When("the interactor is invoked") {
            Then("it forwards the repository content unchanged") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem() shouldBe initial
                }
            }
        }
    }
})

class IdentifyByPhoneNumberImplTest : BehaviorSpec({

    Given("an IdentifyByPhoneNumberImpl wired to a fake AuthManager") {
        val authManager = FakeAuthManager()
        val useCase = IdentifyByPhoneNumberImpl(authManager)

        When("invoked with a 10-digit phone via the public CenterPost contract") {
            Then("it returns Identified with the last-4 in the account id") {
                val result = useCase("5551234567")
                val payload = (result as CenterPostResult.Success).data
                (payload as IdentifyResult.Identified).accountId shouldBe "kiosk-stub-4567"
                authManager.isAuthenticated shouldBe true
            }
        }

        When("invoked with an empty string") {
            Then("the stub falls back to a sentinel suffix") {
                authManager.logout()
                val result = useCase("")
                val payload = (result as CenterPostResult.Success).data
                (payload as IdentifyResult.Identified).accountId shouldBe "kiosk-stub-0000"
            }
        }
    }
})

class IdentifyByQrCodeImplTest : BehaviorSpec({

    Given("an IdentifyByQrCodeImpl wired to a fake AuthManager") {
        val authManager = FakeAuthManager()
        val useCase = IdentifyByQrCodeImpl(authManager)

        When("invoked with a payload") {
            Then("it returns Identified with the payload prefix") {
                val result = useCase("abc123def")
                val payload = (result as CenterPostResult.Success).data
                (payload as IdentifyResult.Identified).accountId shouldBe "kiosk-stub-qr-abc1"
                authManager.isAuthenticated shouldBe true
            }
        }
    }
})

class ContinueAsGuestImplTest : BehaviorSpec({

    Given("a ContinueAsGuestImpl") {
        val useCase = ContinueAsGuestImpl()

        When("invoked") {
            Then("it returns GuestSession") {
                val result = useCase(Unit)
                (result as CenterPostResult.Success).data shouldBe IdentifyResult.GuestSession
            }
        }
    }
})
