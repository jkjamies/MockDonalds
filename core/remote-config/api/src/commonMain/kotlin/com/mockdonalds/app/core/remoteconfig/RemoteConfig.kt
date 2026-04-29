package com.mockdonalds.app.core.remoteconfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

sealed class RemoteConfig<T> {
    abstract val key: String
    abstract val default: T

    data class StringConfig(
        override val key: String,
        override val default: String,
    ) : RemoteConfig<String>()

    data class LongConfig(
        override val key: String,
        override val default: Long,
    ) : RemoteConfig<Long>()

    data class DoubleConfig(
        override val key: String,
        override val default: Double,
    ) : RemoteConfig<Double>()

    data class JsonConfig<T>(
        override val key: String,
        override val default: T,
        val serializer: KSerializer<T>,
    ) : RemoteConfig<T>()
}

inline fun <reified T> jsonConfig(key: String, default: T): RemoteConfig.JsonConfig<T> =
    RemoteConfig.JsonConfig(key = key, default = default, serializer = serializer())
