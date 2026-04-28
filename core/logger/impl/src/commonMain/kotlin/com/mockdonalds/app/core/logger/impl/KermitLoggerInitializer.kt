package com.mockdonalds.app.core.logger.impl

import co.touchlab.kermit.platformLogWriter
import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.core.buildconfig.isDebug
import com.mockdonalds.app.core.logger.LogWriter
import com.mockdonalds.app.core.logger.Logger
import com.mockdonalds.app.core.logger.LoggerInitializer
import com.mockdonalds.app.core.logger.Severity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class KermitLoggerInitializer(
    private val additionalWriters: Set<LogWriter>,
    private val appBuildConfig: AppBuildConfig,
) : LoggerInitializer {
    override fun initialize() {
        Logger.setMinSeverity(if (appBuildConfig.isDebug) Severity.Verbose else Severity.Info)
        Logger.setLogWriters(
            buildList {
                add(platformLogWriter())
                addAll(additionalWriters)
            },
        )
    }
}
