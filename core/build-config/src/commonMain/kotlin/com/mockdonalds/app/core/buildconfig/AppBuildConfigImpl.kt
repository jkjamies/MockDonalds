package com.mockdonalds.app.core.buildconfig

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AppBuildConfigImpl : AppBuildConfig {
    override val appName: String = BuildConfig.APP_NAME
    override val appId: String = BuildConfig.APP_ID
    override val market: String = BuildConfig.MARKET
    override val env: String = BuildConfig.ENV
    override val baseUrl: String = BuildConfig.BASE_URL
    override val cdnUrl: String = BuildConfig.CDN_URL
    override val menuBaseUrl: String = BuildConfig.MENU_BASE_URL
    override val orderBaseUrl: String = BuildConfig.ORDER_BASE_URL
    override val accountBaseUrl: String = BuildConfig.ACCOUNT_BASE_URL
    override val rewardsBaseUrl: String = BuildConfig.REWARDS_BASE_URL
    override val storeBaseUrl: String = BuildConfig.STORE_BASE_URL
    override val locale: String = BuildConfig.LOCALE
    override val currency: String = BuildConfig.CURRENCY
}
