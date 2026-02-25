package com.dnfapps.arrmatey.seerr.api.client

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeDelete
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.client.safePost
import com.dnfapps.arrmatey.client.safePut
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestResponse
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.component.KoinComponent

interface SeerrClient {
    suspend fun testConnection(): NetworkResult<Unit>
    suspend fun getUserInfo(): NetworkResult<SeerrUser>
    suspend fun getRequests(page: Int = 1, pageSize: Int = 20): NetworkResult<RequestResponse>
    suspend fun getMovieDetails(tmdbId: Long): NetworkResult<MovieDetails>
    suspend fun getTvDetails(tmdbId: Long): NetworkResult<TvDetails>
}

class SeerrClientImpl(
    private val instance: Instance,
    private val httpClient: HttpClient
): KoinComponent, SeerrClient {

    private val baseUrl: String
        get() = "${instance.getEffectiveBaseUrl()}/${instance.type.apiBase}"

    override suspend fun testConnection(): NetworkResult<Unit> =
        get(instance.type.testEndpoint)

    override suspend fun getUserInfo(): NetworkResult<SeerrUser> =
        get("auth/me")

    override suspend fun getRequests(
        page: Int,
        pageSize: Int
    ): NetworkResult<RequestResponse> =
        get("request", mapOf(
            "take" to pageSize,
            "skip" to (page - 1) * pageSize
        ))

    override suspend fun getMovieDetails(tmdbId: Long): NetworkResult<MovieDetails> =
        get("movie/$tmdbId")

    override suspend fun getTvDetails(tmdbId: Long): NetworkResult<TvDetails> =
        get("tv/$tmdbId")


    /**
     * Helpers
     */

    private suspend inline fun <reified T> get(
        endpoint: String,
        params: Map<String, Any> = emptyMap()
    ): NetworkResult<T> =
        httpClient.safeGet<T>("$baseUrl/$endpoint") {
            url {
                params.forEach { (key, value) ->
                    parameters.append(key, value.toString())
                }
            }
        }

    private suspend inline fun <reified T, reified R> post(
        endpoint: String,
        body: T
    ): NetworkResult<R> =
        httpClient.safePost<R>("$baseUrl/$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private suspend inline fun <reified T, reified R> put(
        endpoint: String,
        body: T
    ): NetworkResult<R> =
        httpClient.safePut<R>("$baseUrl/$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private suspend inline fun <reified T, reified R> delete(
        endpoint: String,
        body: T,
        params: Map<String, Any> = emptyMap(),
    ): NetworkResult<R> =
        httpClient.safeDelete("$baseUrl/$endpoint") {
            contentType(ContentType.Application.Json)
            url {
                params.forEach { (key, value) ->
                    parameters.append(key, value.toString())
                }
            }
            setBody(body)
        }

    private suspend inline fun <reified T> delete(
        endpoint: String,
        params: Map<String, Any> = emptyMap()
    ): NetworkResult<T> =
        httpClient.safeDelete("$baseUrl/$endpoint") {
            url {
                params.forEach { (key, value) ->
                    parameters.append(key, value.toString())
                }
            }
        }
}