package com.dnfapps.arrmatey.tautulli.api.client

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tautulli.api.model.TautulliActivity
import com.dnfapps.arrmatey.tautulli.api.model.TautulliEnvelope
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHistoryResponse
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHomeStatSection
import com.dnfapps.arrmatey.tautulli.api.model.TautulliLibrary
import com.dnfapps.arrmatey.tautulli.api.model.TautulliMediaType
import com.dnfapps.arrmatey.tautulli.api.model.TautulliMetadata
import com.dnfapps.arrmatey.tautulli.api.model.TautulliServerStatus
import com.dnfapps.arrmatey.tautulli.api.model.TautulliStatsType
import com.dnfapps.arrmatey.tautulli.api.model.TautulliUser
import com.dnfapps.arrmatey.tautulli.api.model.TautulliUserWatchTime
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.json.JsonElement

class TautulliClient(
    val instance: Instance,
    @PublishedApi internal val httpClient: HttpClient,
) {
    @PublishedApi
    internal val baseUrl: String
        get() = instance.getEffectiveBaseUrl().trimEnd('/')

    suspend inline fun <reified T> getCmd(
        cmd: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> {
        val url = "$baseUrl/api/v2"
        val result =
            httpClient.safeGet<TautulliEnvelope<T>>(url) {
                parameter("cmd", cmd)
                header("X-Api-Key", instance.apiKey.value)
                block()
            }
        return when (result) {
            is NetworkResult.Success -> {
                val envelopeResponse = result.data.response
                if (envelopeResponse.result.equals("success", ignoreCase = true) && envelopeResponse.data != null) {
                    NetworkResult.Success(envelopeResponse.data)
                } else {
                    NetworkResult.Error(
                        code = 400,
                        message = envelopeResponse.message ?: "Tautulli error: ${envelopeResponse.result}",
                    )
                }
            }
            is NetworkResult.Error -> NetworkResult.Error(code = result.code, message = result.message, cause = result.cause, errorType = result.errorType)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun testConnection(): NetworkResult<Unit> {
        val result = getCmd<JsonElement>("status")
        return when (result) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> NetworkResult.Error(code = result.code, message = result.message)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun getServerStatus(): NetworkResult<TautulliServerStatus> = getCmd("status")

    suspend fun getActivity(
        sessionId: String? = null,
        sessionKey: String? = null,
    ): NetworkResult<TautulliActivity> = getCmd("get_activity") {
        sessionId?.let { parameter("session_id", it) }
        sessionKey?.let { parameter("session_key", it) }
    }

    suspend fun terminateSession(
        sessionId: String? = null,
        sessionKey: String? = null,
        message: String? = null,
    ): NetworkResult<Unit> {
        val result =
            getCmd<JsonElement>("terminate_session") {
                sessionId?.let { parameter("session_id", it) }
                sessionKey?.let { parameter("session_key", it) }
                message?.takeIf { it.isNotBlank() }?.let { parameter("message", it) }
            }
        return when (result) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> NetworkResult.Error(code = result.code, message = result.message)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun getHomeStats(
        timeRange: Int = 30,
        statsType: TautulliStatsType = TautulliStatsType.Plays,
        statsCount: Int? = null,
    ): NetworkResult<List<TautulliHomeStatSection>> = getCmd("get_home_stats") {
        parameter("time_range", timeRange)
        parameter("stats_type", statsType.value)
        statsCount?.let { parameter("stats_count", it) }
    }

    suspend fun getHistory(
        start: Int? = null,
        length: Int? = null,
        search: String? = null,
        userId: Long? = null,
        ratingKey: String? = null,
        mediaType: TautulliMediaType? = null,
    ): NetworkResult<TautulliHistoryResponse> = getCmd("get_history") {
        start?.let { parameter("start", it) }
        length?.let { parameter("length", it) }
        search?.takeIf { it.isNotBlank() }?.let { parameter("search", it) }
        userId?.let { parameter("user_id", it) }
        ratingKey?.let { parameter("rating_key", it) }
        mediaType?.let { parameter("media_type", it.value) }
    }

    suspend fun getUsers(): NetworkResult<List<TautulliUser>> = getCmd("get_users")

    suspend fun getUser(userId: Long): NetworkResult<TautulliUser> = getCmd("get_user") {
        parameter("user_id", userId)
    }

    suspend fun getUserWatchTimeStats(userId: Long): NetworkResult<List<TautulliUserWatchTime>> = getCmd("get_user_watch_time_stats") {
        parameter("user_id", userId)
    }

    suspend fun getLibraries(): NetworkResult<List<TautulliLibrary>> = getCmd("get_libraries")

    suspend fun getMetadata(ratingKey: String): NetworkResult<TautulliMetadata> = getCmd("get_metadata") {
        parameter("rating_key", ratingKey)
    }
}
