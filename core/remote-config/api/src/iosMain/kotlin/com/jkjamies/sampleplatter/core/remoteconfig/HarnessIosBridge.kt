package com.jkjamies.sampleplatter.core.remoteconfig

/**
 * Kotlin-side contract for the iOS Harness feature-flag SDK.
 *
 * Implemented in Swift (see `iosApp/iosApp/Harness/SwiftHarnessBridge.swift`) and provided
 * to the DI graph via [com.jkjamies.sampleplatter.ProdAppGraph.Factory].
 *
 * This lives in `api`, not `impl`, for the same reason
 * [com.jkjamies.sampleplatter.core.network.AkamaiSensorBridge] does: it is a *contract* the
 * platform implements, not an implementation detail. Keeping it here means the iOS
 * framework exports `core:remote-config:api` rather than `core:remote-config:impl` —
 * so `RemoteConfigProviderImpl`, `RemoteConfigSource` and the multibind aggregator
 * interfaces stay out of the Obj-C header and out of the framework's dead-code-elimination
 * roots.
 */
interface HarnessIosBridge {
    fun boolVariation(key: String, defaultValue: Boolean): Boolean
    fun stringVariation(key: String, defaultValue: String): String
    fun longVariation(key: String, defaultValue: Long): Long
    fun doubleVariation(key: String, defaultValue: Double): Double
    fun jsonVariation(key: String, defaultValueJson: String): String
    fun registerListener(key: String, onChange: () -> Unit): HarnessListenerHandle
}

/** Cancellation handle for a listener registered via [HarnessIosBridge.registerListener]. */
interface HarnessListenerHandle {
    fun cancel()
}
