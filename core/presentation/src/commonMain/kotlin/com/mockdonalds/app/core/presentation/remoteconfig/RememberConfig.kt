package com.mockdonalds.app.core.presentation.remoteconfig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.mockdonalds.app.core.remoteconfig.RemoteConfig
import com.mockdonalds.app.core.remoteconfig.RemoteConfigProvider

@Composable
fun <T> RemoteConfigProvider.rememberConfig(config: RemoteConfig<T>): State<T> {
    val flow = remember(config) { observeConfig(config) }
    return flow.collectAsState(initial = config.default)
}
