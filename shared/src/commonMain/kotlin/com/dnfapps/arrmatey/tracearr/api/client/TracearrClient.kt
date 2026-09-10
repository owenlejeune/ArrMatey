package com.dnfapps.arrmatey.tracearr.api.client

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaDetails
import com.dnfapps.arrmatey.tracearr.api.model.TracearrPeriod
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamsResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUsersResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrViolationsResponse
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

class TracearrClient(
    val instance: Instance,
    private val httpClient: HttpClient,
) {

    private val v2BaseUrl: String
        get() = "${instance.getEffectiveBaseUrl()}/${instance.type.apiBase}/v2"

    private val v1BaseUrl: String
        get() = "${instance.getEffectiveBaseUrl()}/${instance.type.apiBase}/v1"

    suspend fun testConnection(): NetworkResult<Unit> {
        val url = "$v2BaseUrl/${instance.type.testEndpoint}"
        return httpClient.safeGet(url)
    }

    suspend fun getPublicStreams(): NetworkResult<TracearrStreamsResponse> {
        val url = "$v2BaseUrl/public/streams"
        return httpClient.safeGet(url)
    }

    suspend fun getTodayStats(): NetworkResult<TracearrTodayStats> {
        val url = "$v1BaseUrl/public/stats/today"
        return httpClient.safeGet(url)
    }

    suspend fun getViolations(
        page: Int? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrViolationsResponse> {
        val url = "$v1BaseUrl/public/violations"
        return httpClient.safeGet(url) {
            page?.let { parameter("page", it) }
            pageSize?.let { parameter("pageSize", it) }
        }
    }

    suspend fun getActivity(period: TracearrPeriod): NetworkResult<TracearrActivityResponse> {
        val url = "$v1BaseUrl/public/activity"
        return httpClient.safeGet(url) {
            parameter("period", period.value)
        }
    }

    suspend fun getHistory(
        cursor: String? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrHistoryResponse> {
        val url = "$v2BaseUrl/public/history"
        return httpClient.safeGet(url) {
            cursor?.let { parameter("cursor", it) }
            parameter("pageSize", pageSize ?: 100)
        }
    }

    suspend fun getMedia(ref: String): NetworkResult<TracearrMediaDetails> {
        val url = "$v2BaseUrl/public/media/$ref"
        return httpClient.safeGet(url)
    }

    suspend fun getUsers(
        cursor: String? = null,
        pageSize: Int? = null
    ): NetworkResult<TracearrUsersResponse> {
        val url = "$v2BaseUrl/public/users"
        return httpClient.safeGet(url) {
            cursor?.let { parameter("cursor", it) }
            parameter("pageSize", pageSize ?: 100)
            parameter("includeRemoved", false)
        }
    }

    suspend fun getUserDetails(ref: String): NetworkResult<TracearrUserDetail> {
        val url = "$v2BaseUrl/public/users/$ref"
        return httpClient.safeGet(url)
    }

    suspend fun getUserStats(ref: String): NetworkResult<TracearrUserStats> {
        val url = "$v2BaseUrl/public/users/$ref/stats"
        return httpClient.safeGet(url)
    }

    suspend fun getUserHistory(
        ref: String,
        cursor: String? = null,
        pageSize: Int? = null
    ): NetworkResult<TracearrHistoryResponse> {
        val url = "$v2BaseUrl/public/users/$ref/history"
        return httpClient.safeGet(url) {
            cursor?.let { parameter("cursor", it) }
            parameter("pageSize", pageSize ?: 100)
        }
    }
}
