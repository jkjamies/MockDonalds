package com.mockdonalds.app.core.common

sealed interface ResultWrapper<out T> {
    data class Success<T>(val data: T) : ResultWrapper<T>
    data class Error(val exception: Throwable) : ResultWrapper<Nothing>
    data object Loading : ResultWrapper<Nothing>
}
