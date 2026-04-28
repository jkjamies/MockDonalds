package com.mockdonalds.app.core.logger.impl

import com.mockdonalds.app.core.logger.LogWriter
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface LoggerProviders {
    @Multibinds(allowEmpty = true)
    fun additionalLogWriters(): Set<LogWriter>
}
