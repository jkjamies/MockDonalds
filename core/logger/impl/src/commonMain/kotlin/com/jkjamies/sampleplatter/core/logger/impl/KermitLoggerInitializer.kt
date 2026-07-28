package com.jkjamies.sampleplatter.core.logger.impl

import co.touchlab.kermit.platformLogWriter
import com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfig
import com.jkjamies.sampleplatter.core.buildconfig.isDebug
import com.jkjamies.sampleplatter.core.logger.LogWriter
import com.jkjamies.sampleplatter.core.logger.Logger
import com.jkjamies.sampleplatter.core.logger.LoggerInitializer
import com.jkjamies.sampleplatter.core.logger.Severity
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
