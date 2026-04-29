package com.mockdonalds.app.core.remoteconfig

interface RemoteConfigDefinition<T> {
    val config: RemoteConfig<T>
    val description: String
    val owner: String
    val lifecycle: FlagLifecycle
}
