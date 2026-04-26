package com.mockdonalds.app.core.featureflag.impl

interface HarnessIosBridge {
    fun boolVariation(key: String, defaultValue: Boolean): Boolean
    fun registerListener(key: String, onChange: (Boolean) -> Unit): HarnessListenerHandle
}

interface HarnessListenerHandle {
    fun cancel()
}
