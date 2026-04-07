package com.mockdonalds.app.core.centerpost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public class CenterPost internal constructor(
    private val scope: CoroutineScope,
    private val dispatchers: CenterPostDispatchers,
) {
    public operator fun invoke(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = scope.launch(dispatchers.default + context, block = block).also {
        check(!it.isCancelled) { "CenterPost launch failed — scope already cancelled" }
    }

    public fun <T> withResult(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> T,
    ): Deferred<CenterPostResult<T>> = scope.async(dispatchers.default + context) {
        centerPostRunCatching { block() }
    }.also {
        check(!it.isCancelled) { "CenterPost launch failed — scope already cancelled" }
    }
}

@Composable
public fun rememberCenterPost(dispatchers: CenterPostDispatchers): CenterPost {
    val scope = rememberCoroutineScope()
    return remember(dispatchers) { CenterPost(scope, dispatchers) }
}
