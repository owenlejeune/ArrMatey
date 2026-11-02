package com.dnfapps.arrmatey.mediaserver.client

import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserAuthRequest
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserAuthResult
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserBaseItem
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserItemsResult
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserSystemInfo
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeDelete
import com.dnfapps.networking.safeGet
import com.dnfapps.networking.safePost
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class JellyfinClient(
    val integration: MediaServerIntegration,
    private val httpClient: HttpClient,
) {
    private val baseUrl: String
        get() = integration.getEffectiveBaseUrl().trimEnd('/')

    private val token: String
        get() = integration.token.value

    private val clientIdentifier: String
        get() = integration.clientIdentifier

    private fun buildAuthHeader(tokenOverride: String? = null): String {
        val activeToken = tokenOverride ?: token
        val tokenPart = if (activeToken.isNotEmpty()) """, Token="$activeToken"""" else ""
        return """MediaBrowser Client="ArrMatey", Device="ArrMatey", DeviceId="$clientIdentifier", Version="1.0.0"$tokenPart"""
    }

    suspend fun authenticate(request: MediaBrowserAuthRequest): NetworkResult<MediaBrowserAuthResult> = httpClient.safePost("$baseUrl/Users/AuthenticateByName") {
        header("Authorization", buildAuthHeader())
        header("Accept", "application/json")
        contentType(ContentType.Application.Json)
        setBody(request)
    }

    suspend fun getUserViews(userId: String): NetworkResult<MediaBrowserItemsResult> = httpClient.safeGet("$baseUrl/Users/$userId/Views") {
        header("Authorization", buildAuthHeader())
        header("Accept", "application/json")
    }

    suspend fun getLibraryItems(
        userId: String,
        parentId: String,
        startIndex: Int? = 0,
        limit: Int? = 50,
        includeItemTypes: String? = null,
        sortBy: String = "SortName",
        sortOrder: String = "Ascending",
        fields: String = "Overview,PrimaryImageAspectRatio,UserData,ProviderIds,MediaSources",
    ): NetworkResult<MediaBrowserItemsResult> = httpClient.safeGet("$baseUrl/Users/$userId/Items") {
        header("Authorization", buildAuthHeader())
        header("Accept", "application/json")
        parameter("ParentId", parentId)
        if (startIndex != null) parameter("StartIndex", startIndex)
        if (limit != null) parameter("Limit", limit)
        if (includeItemTypes != null) parameter("IncludeItemTypes", includeItemTypes)
        parameter("SortBy", sortBy)
        parameter("SortOrder", sortOrder)
        parameter("Fields", fields)
        parameter("Recursive", true)
    }

    suspend fun getItemDetails(userId: String, itemId: String): NetworkResult<MediaBrowserBaseItem> = httpClient.safeGet("$baseUrl/Users/$userId/Items/$itemId") {
        header("Authorization", buildAuthHeader())
        header("Accept", "application/json")
    }

    suspend fun getResumeItems(userId: String, limit: Int = 20): NetworkResult<MediaBrowserItemsResult> = httpClient.safeGet("$baseUrl/Users/$userId/Items/Resume") {
        header("Authorization", buildAuthHeader())
        header("Accept", "application/json")
        parameter("Limit", limit)
        parameter("Fields", "Overview,UserData,PrimaryImageAspectRatio,ProviderIds,SeriesName")
    }

    suspend fun search(userId: String, query: String, limit: Int = 30): NetworkResult<MediaBrowserItemsResult> = httpClient.safeGet("$baseUrl/Users/$userId/Items") {
        header("Authorization", buildAuthHeader())
        header("Accept", "application/json")
        parameter("SearchTerm", query)
        parameter("Limit", limit)
        parameter("Recursive", true)
        parameter("IncludeItemTypes", "Movie,Series,Episode")
        parameter("Fields", "Overview,UserData,ProviderIds")
    }

    suspend fun getShowEpisodes(userId: String, showId: String): NetworkResult<MediaBrowserItemsResult> = httpClient.safeGet("$baseUrl/Shows/$showId/Episodes") {
        header("Authorization", buildAuthHeader())
        header("Accept", "application/json")
        parameter("UserId", userId)
        parameter("Fields", "Overview,UserData,PrimaryImageAspectRatio,ProviderIds")
    }

    suspend fun markWatched(userId: String, itemId: String, watched: Boolean): NetworkResult<Unit> = if (watched) {
        httpClient.safePost("$baseUrl/Users/$userId/PlayedItems/$itemId") {
            header("Authorization", buildAuthHeader())
            header("Accept", "application/json")
        }
    } else {
        httpClient.safeDelete("$baseUrl/Users/$userId/PlayedItems/$itemId") {
            header("Authorization", buildAuthHeader())
            header("Accept", "application/json")
        }
    }

    suspend fun getSystemInfo(): NetworkResult<MediaBrowserSystemInfo> = httpClient.safeGet("$baseUrl/System/Info") {
        header("Authorization", buildAuthHeader())
        header("Accept", "application/json")
    }
}
