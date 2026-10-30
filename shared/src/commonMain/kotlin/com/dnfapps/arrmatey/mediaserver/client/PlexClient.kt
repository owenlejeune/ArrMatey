package com.dnfapps.arrmatey.mediaserver.client

import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.PlexDirectoryListing
import com.dnfapps.arrmatey.mediaserver.model.PlexMediaListing
import com.dnfapps.arrmatey.mediaserver.model.PlexRootContainer
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class PlexClient(
    val integration: MediaServerIntegration,
    private val httpClient: HttpClient,
) {
    private val baseUrl: String
        get() = integration.getEffectiveBaseUrl().trimEnd('/')

    private val token: String
        get() = integration.token.value

    private val clientIdentifier: String
        get() = integration.clientIdentifier

    suspend fun getSections(): NetworkResult<PlexRootContainer<PlexDirectoryListing>> = httpClient.safeGet("$baseUrl/library/sections") {
        header("X-Plex-Token", token)
        header("X-Plex-Client-Identifier", clientIdentifier)
        header("Accept", "application/json")
        parameter("X-Plex-Token", token)
        parameter("X-Plex-Client-Identifier", clientIdentifier)
    }

    suspend fun getSectionMedia(
        sectionId: String,
        start: Int = 0,
        size: Int = 50,
        filterType: Int? = null,
        sort: String? = null,
    ): NetworkResult<PlexRootContainer<PlexMediaListing>> = httpClient.safeGet("$baseUrl/library/sections/$sectionId/all") {
        header("X-Plex-Token", token)
        header("X-Plex-Client-Identifier", clientIdentifier)
        header("X-Plex-Container-Start", start.toString())
        header("X-Plex-Container-Size", size.toString())
        header("Accept", "application/json")
        parameter("X-Plex-Token", token)
        parameter("X-Plex-Client-Identifier", clientIdentifier)
        parameter("X-Plex-Container-Start", start)
        parameter("X-Plex-Container-Size", size)
        if (filterType != null) parameter("type", filterType)
        if (sort != null) parameter("sort", sort)
    }

    suspend fun getItemMetadata(ratingKey: String): NetworkResult<PlexRootContainer<PlexMediaListing>> = httpClient.safeGet("$baseUrl/library/metadata/$ratingKey") {
        header("X-Plex-Token", token)
        header("X-Plex-Client-Identifier", clientIdentifier)
        header("Accept", "application/json")
        parameter("X-Plex-Token", token)
        parameter("X-Plex-Client-Identifier", clientIdentifier)
    }

    suspend fun getItemChildren(ratingKey: String): NetworkResult<PlexRootContainer<PlexMediaListing>> = httpClient.safeGet("$baseUrl/library/metadata/$ratingKey/children") {
        header("X-Plex-Token", token)
        header("X-Plex-Client-Identifier", clientIdentifier)
        header("Accept", "application/json")
        parameter("X-Plex-Token", token)
        parameter("X-Plex-Client-Identifier", clientIdentifier)
    }

    suspend fun getOnDeck(): NetworkResult<PlexRootContainer<PlexMediaListing>> = httpClient.safeGet("$baseUrl/library/onDeck") {
        header("X-Plex-Token", token)
        header("X-Plex-Client-Identifier", clientIdentifier)
        header("Accept", "application/json")
        parameter("X-Plex-Token", token)
        parameter("X-Plex-Client-Identifier", clientIdentifier)
    }

    suspend fun search(query: String): NetworkResult<PlexRootContainer<PlexMediaListing>> = httpClient.safeGet("$baseUrl/search") {
        header("X-Plex-Token", token)
        header("X-Plex-Client-Identifier", clientIdentifier)
        header("Accept", "application/json")
        parameter("X-Plex-Token", token)
        parameter("X-Plex-Client-Identifier", clientIdentifier)
        parameter("query", query)
    }

    suspend fun markWatched(ratingKey: String, watched: Boolean): NetworkResult<Unit> {
        val endpoint = if (watched) "/:/scrobble" else "/:/unscrobble"
        return httpClient.safeGet("$baseUrl$endpoint") {
            header("X-Plex-Token", token)
            header("X-Plex-Client-Identifier", clientIdentifier)
            parameter("X-Plex-Token", token)
            parameter("X-Plex-Client-Identifier", clientIdentifier)
            parameter("key", ratingKey)
            parameter("identifier", "com.plexapp.plugins.library")
        }
    }

    suspend fun refreshSection(sectionId: String): NetworkResult<Unit> = httpClient.safeGet("$baseUrl/library/sections/$sectionId/refresh") {
        header("X-Plex-Token", token)
        header("X-Plex-Client-Identifier", clientIdentifier)
        parameter("X-Plex-Token", token)
        parameter("X-Plex-Client-Identifier", clientIdentifier)
    }
}
