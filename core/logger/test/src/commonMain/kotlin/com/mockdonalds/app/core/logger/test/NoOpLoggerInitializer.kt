package com.mockdonalds.app.core.logger.test

import com.mockdonalds.app.core.logger.LoggerInitializer

class NoOpLoggerInitializer : LoggerInitializer {
    override fun initialize() = Unit
}
