package com.jkjamies.sampleplatter.core.metro

import com.jkjamies.sampleplatter.core.analytics.AnalyticsDispatcher
import com.jkjamies.sampleplatter.core.auth.AuthManager
import com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfig
import com.jkjamies.sampleplatter.core.logger.LoggerInitializer
import com.slack.circuit.foundation.Circuit

interface AppGraph {
    val circuit: Circuit
    val analyticsDispatcher: AnalyticsDispatcher
    val authManager: AuthManager
    val appBuildConfig: AppBuildConfig
    val loggerInitializer: LoggerInitializer
}
