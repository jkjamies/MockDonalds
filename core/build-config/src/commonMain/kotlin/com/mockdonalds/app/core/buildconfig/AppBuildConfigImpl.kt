package com.mockdonalds.app.core.buildconfig

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AppBuildConfigImpl : AppBuildConfig {
    override val appName: String = BuildConfig.APP_NAME
    override val market: String = BuildConfig.MARKET
    override val env: String = BuildConfig.ENV
    override val baseUrl: String = BuildConfig.BASE_URL
    override val cdnUrl: String = BuildConfig.CDN_URL
    override val locale: String = BuildConfig.LOCALE
    override val currency: String = BuildConfig.CURRENCY
}
