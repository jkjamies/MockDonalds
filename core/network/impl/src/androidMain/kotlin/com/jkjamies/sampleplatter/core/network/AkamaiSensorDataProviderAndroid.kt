package com.jkjamies.sampleplatter.core.network

import android.app.Application
import com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

/**
 * Android binding for [SensorDataProvider], wired to the Akamai Bot Manager Mobile SDK.
 *
 * POC default returns empty so the sensor header is omitted. To activate the real SDK:
 *  1. Drop `AkamaiBMP-<ver>.aar` into `core/network/impl/libs/`.
 *  2. Add `implementation(files("libs/AkamaiBMP-<ver>.aar"))` to the `androidMain` deps.
 *  3. Replace this class body with:
 *     ```
 *     init {
 *         CYFMonitor.initializeSDK(application, appBuildConfig.baseUrl)
 *         CYFMonitor.enableBackground()
 *     }
 *     override suspend fun currentSensorData(): String =
 *         CYFMonitor.getSensorData().orEmpty()
 *     ```
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class AkamaiSensorDataProviderAndroid(
    @Suppress("UnusedPrivateProperty") private val application: Application,
    @Suppress("UnusedPrivateProperty") private val appBuildConfig: AppBuildConfig,
) : SensorDataProvider {
    override suspend fun currentSensorData(): String = ""
}
