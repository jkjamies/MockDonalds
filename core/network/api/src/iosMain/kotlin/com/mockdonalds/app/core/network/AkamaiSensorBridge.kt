package com.mockdonalds.app.core.network

/**
 * Kotlin-side contract for the iOS Akamai Bot Manager SDK.
 *
 * Implemented in Swift (see `iosApp/iosApp/Akamai/SwiftAkamaiSensorBridge.swift`)
 * and provided to the DI graph via [com.mockdonalds.app.ProdAppGraph.Factory].
 * Mirrors the pattern used for Harness feature flags.
 */
interface AkamaiSensorBridge {
    fun currentSensorData(): String
}
