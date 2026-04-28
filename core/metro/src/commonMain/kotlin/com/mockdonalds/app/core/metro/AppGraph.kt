package com.mockdonalds.app.core.metro

import com.mockdonalds.app.core.analytics.AnalyticsDispatcher
import com.mockdonalds.app.core.auth.AuthManager
import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.core.logger.LoggerInitializer
import com.slack.circuit.foundation.Circuit

interface AppGraph {
    val circuit: Circuit
    val analyticsDispatcher: AnalyticsDispatcher
    val authManager: AuthManager
    val appBuildConfig: AppBuildConfig
    val loggerInitializer: LoggerInitializer
}
