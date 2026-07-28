package com.jkjamies.sampleplatter.core.presentation.remoteconfig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.jkjamies.sampleplatter.core.remoteconfig.RemoteConfig
import com.jkjamies.sampleplatter.core.remoteconfig.RemoteConfigProvider

@Composable
fun <T> RemoteConfigProvider.rememberConfig(config: RemoteConfig<T>): State<T> {
    val flow = remember(config) { observeConfig(config) }
    return flow.collectAsState(initial = config.default)
}
