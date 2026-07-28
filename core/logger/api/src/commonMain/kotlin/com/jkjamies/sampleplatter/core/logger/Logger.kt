package com.jkjamies.sampleplatter.core.logger

typealias Logger = co.touchlab.kermit.Logger
typealias Severity = co.touchlab.kermit.Severity
typealias LogWriter = co.touchlab.kermit.LogWriter

fun featureLogger(tag: String): Logger = Logger.withTag(tag)

interface LoggerInitializer {
    fun initialize()
}
