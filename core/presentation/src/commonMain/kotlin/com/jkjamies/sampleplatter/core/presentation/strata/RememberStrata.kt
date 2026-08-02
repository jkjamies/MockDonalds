package com.jkjamies.sampleplatter.core.presentation.strata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.jkjamies.sampleplatter.core.strata.Strata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers

@Composable
public fun rememberStrata(dispatchers: StrataDispatchers): Strata {
    val scope = rememberCoroutineScope()
    return remember(dispatchers) { Strata(scope, dispatchers) }
}
