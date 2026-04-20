package com.mockdonalds.app.core.buildconfig.test

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class FakeAppBuildConfig : AppBuildConfig {
    override var appName: String = "MockDonalds"
    override var appId: String = "us-mockdonalds-mobile-int"
    override var market: String = "us"
    override var env: String = "int"
    override var buildType: String = "debug"
    override var baseUrl: String = "https://int-api.mockdonalds.com"
    override var cdnUrl: String = "https://int-cdn.mockdonalds.com"
    override var menuBaseUrl: String = "https://int-menu-api.mockdonalds.com"
    override var orderBaseUrl: String = "https://int-order-api.mockdonalds.com"
    override var accountBaseUrl: String = "https://int-account-api.mockdonalds.com"
    override var rewardsBaseUrl: String = "https://int-rewards-api.mockdonalds.com"
    override var storeBaseUrl: String = "https://int-stores-api.mockdonalds.com"
    override var locale: String = "en-US"
    override var currency: String = "USD"
}
