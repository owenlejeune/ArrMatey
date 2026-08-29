package com.dnfapps.networking

sealed class NetworkResult<out T> {
    data object Loading : NetworkResult<Nothing>()

    data class Success<out T>(
        val data: T,
    ) : NetworkResult<T>()

    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val cause: Throwable? = null,
        val errorType: ErrorType = ErrorType.Unexpected,
    ) : NetworkResult<Nothing>()

    fun <R> map(transform: (T) -> R): NetworkResult<R> =
        when (this) {
            is Loading -> Loading
            is Error -> Error(code, message, cause, errorType)
            is Success -> Success(transform(data))
        }
}

fun <T, R> NetworkResult<List<T>>.mapValues(transform: (T) -> R): NetworkResult<List<R>> =
    when (this) {
        is NetworkResult.Loading -> NetworkResult.Loading
        is NetworkResult.Error -> NetworkResult.Error(code, message, cause, errorType)
        is NetworkResult.Success -> NetworkResult.Success(data = data.map(transform))
    }

fun <T> NetworkResult<List<T>>.filterValues(predicate: (T) -> Boolean): NetworkResult<List<T>> =
    when (this) {
        is NetworkResult.Loading -> NetworkResult.Loading
        is NetworkResult.Error -> NetworkResult.Error(code, message, cause, errorType)
        is NetworkResult.Success -> NetworkResult.Success(data = data.filter(predicate))
    }

suspend fun <T> NetworkResult<T>.onSuccess(action: suspend (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

suspend fun <T> NetworkResult<T>.onError(action: suspend (code: Int?, message: String?, cause: Throwable?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(code, message, cause)
    return this
}

fun <T> NetworkResult<T>.asLoading(): NetworkResult.Loading? = this as? NetworkResult.Loading

fun <T> NetworkResult<T>.asSuccess(): NetworkResult.Success<T>? = this as? NetworkResult.Success

fun <T> NetworkResult<T>.asError(): NetworkResult.Error? = this as? NetworkResult.Error
