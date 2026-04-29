package com.mockdonalds.app.core.remoteconfig.impl

interface HarnessIosBridge {
    fun boolVariation(key: String, defaultValue: Boolean): Boolean
    fun stringVariation(key: String, defaultValue: String): String
    fun longVariation(key: String, defaultValue: Long): Long
    fun doubleVariation(key: String, defaultValue: Double): Double
    fun jsonVariation(key: String, defaultValueJson: String): String
    fun registerListener(key: String, onChange: () -> Unit): HarnessListenerHandle
}

interface HarnessListenerHandle {
    fun cancel()
}
