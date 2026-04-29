package com.mockdonalds.app.core.presentation.remoteconfig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.mockdonalds.app.core.remoteconfig.FeatureFlag
import com.mockdonalds.app.core.remoteconfig.RemoteConfigProvider

@Composable
fun RemoteConfigProvider.rememberFlag(flag: FeatureFlag): State<Boolean> {
    val flow = remember(flag) { observe(flag) }
    return flow.collectAsState(initial = flag.defaultValue)
}
