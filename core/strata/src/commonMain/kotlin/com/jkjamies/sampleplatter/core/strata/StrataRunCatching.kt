package com.jkjamies.sampleplatter.core.strata

import kotlin.coroutines.cancellation.CancellationException

public inline fun <R> strataRunCatching(block: () -> R): StrataResult<R> {
    return try {
        StrataResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: StrataException) {
        StrataResult.Failure(e)
    } catch (e: Throwable) {
        StrataResult.Failure(StrataExecutionException(e))
    }
}
