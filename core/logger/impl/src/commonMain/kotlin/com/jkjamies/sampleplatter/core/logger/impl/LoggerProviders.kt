package com.jkjamies.sampleplatter.core.logger.impl

import com.jkjamies.sampleplatter.core.logger.LogWriter
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface LoggerProviders {
    @Multibinds(allowEmpty = true)
    fun additionalLogWriters(): Set<LogWriter>
}
