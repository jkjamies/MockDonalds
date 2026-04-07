package com.mockdonalds.app.core.centerpost

import kotlin.coroutines.cancellation.CancellationException

public inline fun <R> centerPostRunCatching(block: () -> R): CenterPostResult<R> {
    return try {
        CenterPostResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: CenterPostException) {
        CenterPostResult.Failure(e)
    } catch (e: Throwable) {
        CenterPostResult.Failure(CenterPostExecutionException(e))
    }
}
