package com.jkjamies.sampleplatter.core.centerpost

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public class CenterPost(
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
