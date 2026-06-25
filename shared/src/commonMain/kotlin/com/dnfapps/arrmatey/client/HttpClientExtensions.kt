package com.dnfapps.arrmatey.client

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put

suspend inline fun <reified T> HttpClient.safeGet(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): NetworkResult<T> {
    return safeCall {
        get(url, builder).body()
    }
}

suspend inline fun <reified T> HttpClient.safePost(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): NetworkResult<T> {
    return safeCall {
        post(url, builder).body()
    }
}

suspend inline fun <reified T> HttpClient.safePut(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): NetworkResult<T> {
    return safeCall {
        put(url, builder).body()
    }
}

suspend inline fun <reified T> HttpClient.safePatch(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): NetworkResult<T> {
    return safeCall {
        patch(url, builder).body()
    }
}

suspend inline fun <reified T> HttpClient.safeDelete(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): NetworkResult<T> {
    return safeCall {
        delete(url, builder).body()
    }
}

suspend inline fun <reified T> HttpClient.safeCall(
    crossinline block: suspend HttpClient.() -> T
): NetworkResult<T> {
    return try {
        val data = block(this)
        NetworkResult.Success(data)
    } catch (e: ClientRequestException) {
        // 4xx
        val status = e.response.status
        NetworkResult.Error(code = status.value, message = status.description, errorType = ErrorType.Http)
    } catch (e: ServerResponseException) {
        // 5xx
        val status = e.response.status
        NetworkResult.Error(code = status.value, message = status.description, errorType = ErrorType.Http)
    } catch (e: ResponseException) {
        // Any other non‑2xx mapped by Ktor into ResponseException
        val status = e.response.status
        NetworkResult.Error(code = status.value, message = status.description, errorType = ErrorType.Http)
    } catch (e: ConnectTimeoutException) {
        NetworkResult.Error(message = "Request timed out", cause = e, errorType = ErrorType.Timeout)
    } catch (e: HttpRequestTimeoutException) {
        NetworkResult.Error(message = "Request timed out", cause = e, errorType = ErrorType.Timeout)
    } catch (e: Throwable) {
        if (e.isTimeoutError()) {
            NetworkResult.Error(message = "Request timed out", cause = e, errorType = ErrorType.Timeout)
        } else if (e.isNoConnectionError()) {
            NetworkResult.Error(message = e.cause?.message ?: e.message, cause = e, errorType = ErrorType.Network)
        } else {
            NetworkResult.Error(cause = e, errorType = ErrorType.Unexpected)
        }
    }
}

expect fun Throwable.isNoConnectionError(): Boolean

expect fun Throwable.isTimeoutError(): Boolean