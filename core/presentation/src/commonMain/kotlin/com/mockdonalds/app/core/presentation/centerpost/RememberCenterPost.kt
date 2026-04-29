package com.mockdonalds.app.core.presentation.centerpost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mockdonalds.app.core.centerpost.CenterPost
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers

@Composable
public fun rememberCenterPost(dispatchers: CenterPostDispatchers): CenterPost {
    val scope = rememberCoroutineScope()
    return remember(dispatchers) { CenterPost(scope, dispatchers) }
}
