package com.mockdonalds.app.core.featureflag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

@Composable
fun FeatureFlagProvider.rememberFlag(flag: FeatureFlag): State<Boolean> {
    val flow = remember(flag) { observe(flag) }
    return flow.collectAsState(initial = flag.defaultValue)
}
