package com.mockdonalds.app.core.centerpost

public sealed interface CenterPostResult<out T> {

    public data class Success<T>(val data: T) : CenterPostResult<T>
    public data class Failure(val error: CenterPostException) : CenterPostResult<Nothing>

    public fun onSuccess(action: (T) -> Unit): CenterPostResult<T> = when (this) {
        is Success -> { action(data); this }
        is Failure -> this
    }

    public fun onFailure(action: (CenterPostException) -> Unit): CenterPostResult<T> = when (this) {
        is Success -> this
        is Failure -> { action(error); this }
    }

    public fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    public fun <R> map(transform: (T) -> R): CenterPostResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    public fun <R> flatMap(transform: (T) -> CenterPostResult<R>): CenterPostResult<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    public fun <R> fold(onSuccess: (T) -> R, onFailure: (CenterPostException) -> R): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(error)
    }
}

public fun <T> CenterPostResult<T>.getOrDefault(default: T): T = when (this) {
    is CenterPostResult.Success -> data
    is CenterPostResult.Failure -> default
}

public fun <T> CenterPostResult<T>.getOrElse(transform: (CenterPostException) -> T): T = when (this) {
    is CenterPostResult.Success -> data
    is CenterPostResult.Failure -> transform(error)
}

public suspend fun <T> CenterPostResult<T>.recover(
    transform: suspend (CenterPostException) -> CenterPostResult<T>,
): CenterPostResult<T> = when (this) {
    is CenterPostResult.Success -> this
    is CenterPostResult.Failure -> transform(error)
}
