package com.dnfapps.arrmatey.seerr.api.client

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeDelete
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.client.safePost
import com.dnfapps.arrmatey.client.safePut
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.IssueBody
import com.dnfapps.arrmatey.seerr.api.model.IssuesResponse
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestResponse
import com.dnfapps.arrmatey.seerr.api.model.RottenTomatoesRating
import com.dnfapps.arrmatey.seerr.api.model.Season
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.Service
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.component.KoinComponent

interface SeerrClient {
    suspend fun testConnection(): NetworkResult<Unit>
    suspend fun getUserInfo(): NetworkResult<SeerrUser>
    suspend fun getRequests(page: Int = 1, pageSize: Int = 100): NetworkResult<RequestResponse>
    suspend fun getMovieDetails(tmdbId: Long): NetworkResult<MovieDetails>
    suspend fun getTvDetails(tmdbId: Long): NetworkResult<TvDetails>
    suspend fun setRequestStatus(
        requestId: Long,
        status: ApprovalStatus,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null
    ): NetworkResult<MediaRequest>
    suspend fun deleteRequest(requestId: Long): NetworkResult<Unit>
    suspend fun deleteMediaFile(mediaId: Long, is4k: Boolean): NetworkResult<Unit>
    suspend fun getMovieRatings(mediaId: Long): NetworkResult<CombinedRatings>
    suspend fun getTvRatings(mediaId: Long): NetworkResult<RottenTomatoesRating>
    suspend fun getSeasonDetails(mediaId: Long, seasonNumber: Int): NetworkResult<Season>
    suspend fun getRadarrServices(): NetworkResult<List<Service>>
    suspend fun getSonarrServices(): NetworkResult<List<Service>>
    suspend fun getRadarrDetails(id: Long): NetworkResult<ServiceDetails>
    suspend fun getSonarrDetails(id: Long): NetworkResult<ServiceDetails>
    suspend fun getIssues(page: Int = 1, pageSize: Int = 100): NetworkResult<IssuesResponse>
    suspend fun submitIssue(issue: IssueBody): NetworkResult<Issue>
    suspend fun submitIssueComment(issueId: Long, comment: String): NetworkResult<Issue>
    suspend fun getIssueDetails(issueId: Long): NetworkResult<Issue>
    suspend fun closeIssue(issueId: Long): NetworkResult<Unit>
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
            "skip" to (page - 1) * pageSize,
            "filter" to "pending"
        ))

    override suspend fun getMovieDetails(tmdbId: Long): NetworkResult<MovieDetails> =
        get("movie/$tmdbId")

    override suspend fun getTvDetails(tmdbId: Long): NetworkResult<TvDetails> =
        get("tv/$tmdbId")

    override suspend fun setRequestStatus(
        requestId: Long,
        status: ApprovalStatus,
        profileId: Long?,
        rootFolder: String?,
        languageProfileId: Long?,
        seasons: List<Int>?
    ): NetworkResult<MediaRequest> =
        post("request/$requestId/${status.name.lowercase()}", buildJsonObject {
            profileId?.let { put("profileId", it) }
            rootFolder?.let { put("rootFolder", it) }
            languageProfileId?.let { put("languageProfileId", it) }
            seasons?.let { put("seasons", JsonArray(it.map { s -> JsonPrimitive(s) })) }
        })

    override suspend fun deleteRequest(requestId: Long): NetworkResult<Unit> =
        delete("request/$requestId")

    override suspend fun deleteMediaFile(mediaId: Long, is4k: Boolean): NetworkResult<Unit> =
        delete("media/$mediaId", mapOf("is4k" to is4k))

    override suspend fun getMovieRatings(mediaId: Long): NetworkResult<CombinedRatings> =
        get("movie/$mediaId/ratingscombined")

    override suspend fun getTvRatings(mediaId: Long): NetworkResult<RottenTomatoesRating> =
        get("tv/$mediaId/ratings")

    override suspend fun getSeasonDetails(mediaId: Long, seasonNumber: Int): NetworkResult<Season> =
        get("tv/$mediaId/season/$seasonNumber")

    override suspend fun getRadarrServices(): NetworkResult<List<Service>> =
        get("service/radarr")

    override suspend fun getSonarrServices(): NetworkResult<List<Service>> =
        get("service/sonarr")

    override suspend fun getRadarrDetails(id: Long): NetworkResult<ServiceDetails> =
        get("service/radarr/$id")

    override suspend fun getSonarrDetails(id: Long): NetworkResult<ServiceDetails> =
        get("service/sonarr/$id")

    override suspend fun getIssues(page: Int, pageSize: Int): NetworkResult<IssuesResponse> =
        get("issue", mapOf(
            "take" to pageSize,
            "skip" to (page - 1) * pageSize,
            "filter" to "open"
        ))

    override suspend fun submitIssue(issue: IssueBody): NetworkResult<Issue> =
        post("issue", issue)

    override suspend fun submitIssueComment(issueId: Long, comment: String): NetworkResult<Issue> =
        post("issue/$issueId/comment", buildJsonObject {
            put("message", comment)
        })

    override suspend fun getIssueDetails(issueId: Long): NetworkResult<Issue> =
        get("issue/$issueId")

    override suspend fun closeIssue(issueId: Long): NetworkResult<Unit> =
        delete("issue/$issueId")


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

    private suspend inline fun <reified T> post(
        endpoint: String
    ): NetworkResult<T> =
        httpClient.safePost<T>("$baseUrl/$endpoint") {
            contentType(ContentType.Application.Json)
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