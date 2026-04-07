package com.mockdonalds.app.core.test

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.engine.concurrency.SpecExecutionMode

open class KotestProjectConfig : AbstractProjectConfig() {
    override val specExecutionMode = SpecExecutionMode.LimitedConcurrency(4)
}
