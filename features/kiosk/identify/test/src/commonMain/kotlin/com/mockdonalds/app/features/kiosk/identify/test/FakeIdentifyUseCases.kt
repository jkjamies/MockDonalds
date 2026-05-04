package com.mockdonalds.app.features.kiosk.identify.test

import com.mockdonalds.app.features.kiosk.identify.api.domain.ContinueAsGuest
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyByPhoneNumber
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyByQrCode
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyResult
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class FakeIdentifyByPhoneNumber(
    var nextResult: IdentifyResult = IdentifyResult.Identified("kiosk-fake-phone"),
) : IdentifyByPhoneNumber() {
    override suspend fun doWork(params: String): IdentifyResult = nextResult
}

@ContributesBinding(AppScope::class)
class FakeIdentifyByQrCode(
    var nextResult: IdentifyResult = IdentifyResult.Identified("kiosk-fake-qr"),
) : IdentifyByQrCode() {
    override suspend fun doWork(params: String): IdentifyResult = nextResult
}

@ContributesBinding(AppScope::class)
class FakeContinueAsGuest(
    var nextResult: IdentifyResult = IdentifyResult.GuestSession,
) : ContinueAsGuest() {
    override suspend fun doWork(params: Unit): IdentifyResult = nextResult
}
