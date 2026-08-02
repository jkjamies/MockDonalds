package com.jkjamies.sampleplatter.core.strata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public class Strata(
    private val scope: CoroutineScope,
    private val dispatchers: StrataDispatchers,
) {
    public operator fun invoke(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = scope.launch(dispatchers.default + context, block = block).also {
        check(!it.isCancelled) { "Strata launch failed — scope already cancelled" }
    }

    public fun <T> withResult(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> T,
    ): Deferred<StrataResult<T>> = scope.async(dispatchers.default + context) {
        strataRunCatching { block() }
    }.also {
        check(!it.isCancelled) { "Strata launch failed — scope already cancelled" }
    }
}
