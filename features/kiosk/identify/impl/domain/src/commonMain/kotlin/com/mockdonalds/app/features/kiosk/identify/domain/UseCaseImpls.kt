package com.mockdonalds.app.features.kiosk.identify.domain

import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.features.kiosk.identify.api.domain.ContinueAsGuest
import com.mockdonalds.app.features.kiosk.identify.api.domain.GetIdentifyContent
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyByPhoneNumber
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyByQrCode
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyContent
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyResult
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class GetIdentifyContentImpl(
    private val repository: IdentifyRepository,
) : GetIdentifyContent() {
    override fun createObservable(params: Unit): Flow<IdentifyContent> = repository.getIdentifyContent()
}

/**
 * Phase-3 stub: deterministic identified result keyed off the last 4 digits of the
 * submitted phone. Calls `AuthManager.login()` so analytics/downstream features see
 * an authenticated session. Real `/v1/kiosk/identify` backend wiring is a follow-up;
 * the use-case surface is stable so the swap is impl-only.
 */
@ContributesBinding(AppScope::class)
class IdentifyByPhoneNumberImpl(
    private val authManager: AuthManager,
) : IdentifyByPhoneNumber() {
    override suspend fun doWork(params: String): IdentifyResult {
        authManager.login()
        val tail = params.takeLast(MinTailDigits).ifEmpty { "0000" }
        return IdentifyResult.Identified(accountId = "kiosk-stub-$tail")
    }
}

/**
 * Phase-3 stub: simulated QR scanner dispatches a synthetic payload from the UI tap.
 * This use case maps it to a deterministic identified result so the downstream flow
 * is exercisable end-to-end without real CameraX hardware.
 */
@ContributesBinding(AppScope::class)
class IdentifyByQrCodeImpl(
    private val authManager: AuthManager,
) : IdentifyByQrCode() {
    override suspend fun doWork(params: String): IdentifyResult {
        authManager.login()
        val truncated = params.take(MinTailDigits)
        return IdentifyResult.Identified(accountId = "kiosk-stub-qr-$truncated")
    }
}

/**
 * Pure local: marks the kiosk session as a guest. Does NOT call `AuthManager.login()` —
 * guests stay unauthenticated, and downstream features should branch on
 * `AuthManager.isAuthenticated` if they need to differentiate.
 */
@ContributesBinding(AppScope::class)
class ContinueAsGuestImpl : ContinueAsGuest() {
    override suspend fun doWork(params: Unit): IdentifyResult = IdentifyResult.GuestSession
}

private const val MinTailDigits = 4
