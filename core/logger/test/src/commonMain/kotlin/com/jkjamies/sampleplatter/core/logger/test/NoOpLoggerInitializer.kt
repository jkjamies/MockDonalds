package com.jkjamies.sampleplatter.core.logger.test

import com.jkjamies.sampleplatter.core.logger.LoggerInitializer

class NoOpLoggerInitializer : LoggerInitializer {
    override fun initialize() = Unit
}
