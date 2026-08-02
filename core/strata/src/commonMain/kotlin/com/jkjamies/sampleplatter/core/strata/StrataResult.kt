package com.jkjamies.sampleplatter.core.strata

public sealed interface StrataResult<out T> {

    public data class Success<T>(val data: T) : StrataResult<T>
    public data class Failure(val error: StrataException) : StrataResult<Nothing>

    public fun onSuccess(action: (T) -> Unit): StrataResult<T> = when (this) {
        is Success -> {
            action(data)
            this
        }
        is Failure -> this
    }

    public fun onFailure(action: (StrataException) -> Unit): StrataResult<T> = when (this) {
        is Success -> this
        is Failure -> {
            action(error)
            this
        }
    }

    public fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    public fun <R> map(transform: (T) -> R): StrataResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    public fun <R> flatMap(transform: (T) -> StrataResult<R>): StrataResult<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    public fun <R> fold(onSuccess: (T) -> R, onFailure: (StrataException) -> R): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(error)
    }
}

public fun <T> StrataResult<T>.getOrDefault(default: T): T = when (this) {
    is StrataResult.Success -> data
    is StrataResult.Failure -> default
}

public fun <T> StrataResult<T>.getOrElse(transform: (StrataException) -> T): T = when (this) {
    is StrataResult.Success -> data
    is StrataResult.Failure -> transform(error)
}

public suspend fun <T> StrataResult<T>.recover(
    transform: suspend (StrataException) -> StrataResult<T>,
): StrataResult<T> = when (this) {
    is StrataResult.Success -> this
    is StrataResult.Failure -> transform(error)
}
