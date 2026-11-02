package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.mediaserver.client.PlexClient
import com.dnfapps.arrmatey.mediaserver.client.PlexConnectionUtils
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.PlexMetadataItem
import com.dnfapps.arrmatey.mediaserver.model.PlexServerResource
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class PlexRepository(
    private val client: PlexClient,
    private val onUrlUpdated: (suspend (String) -> Unit)? = null,
) : MediaServerRepository {
    override val serverType: MediaServerType = MediaServerType.Plex
    override val integration: MediaServerIntegration = client.integration

    private val baseUrl: String
        get() = client.baseUrl

    override suspend fun getLibraries(): NetworkResult<List<UnifiedMediaLibrary>> {
        var res = client.getSections()
        if (res is NetworkResult.Error && isRecoverableNetworkError(res)) {
            val healed = autoHealFromPlexTv()
            if (healed) {
                res = client.getSections()
            }
        }
        return when (res) {
            is NetworkResult.Success -> {
                val libs = res.data.mediaContainer.directories.orEmpty().map { dir ->
                    UnifiedMediaLibrary(
                        id = dir.key,
                        title = dir.title,
                        type = MediaType.fromString(dir.type),
                        serverType = MediaServerType.Plex,
                    )
                }
                NetworkResult.Success(libs)
            }
            is NetworkResult.Error -> res
            is NetworkResult.Loading -> res
        }
    }

    override suspend fun getLibraryContents(
        libraryId: String,
        page: Int,
        pageSize: Int,
        sort: String?,
    ): NetworkResult<List<UnifiedMediaItem>> {
        if (pageSize <= 0) {
            return getAllLibraryContents(libraryId, sort)
        }
        var res = client.getSectionMedia(sectionId = libraryId, start = page * pageSize, size = pageSize, sort = sort)
        if (res is NetworkResult.Error && isRecoverableNetworkError(res)) {
            val healed = autoHealFromPlexTv()
            if (healed) {
                res = client.getSectionMedia(sectionId = libraryId, start = page * pageSize, size = pageSize, sort = sort)
            }
        }
        return when (res) {
            is NetworkResult.Success -> {
                val items = res.data.mediaContainer.metadata.orEmpty()
                    .filter { !it.ratingKey.isNullOrBlank() }
                    .map { it.toUnifiedMediaItem(libraryId) }
                NetworkResult.Success(items)
            }
            is NetworkResult.Error -> res
            is NetworkResult.Loading -> res
        }
    }

    override suspend fun getAllLibraryContents(
        libraryId: String,
        sort: String?,
    ): NetworkResult<List<UnifiedMediaItem>> {
        var res = client.getSectionMedia(sectionId = libraryId, start = null, size = null, sort = sort)
        if (res is NetworkResult.Error && isRecoverableNetworkError(res)) {
            val healed = autoHealFromPlexTv()
            if (healed) {
                res = client.getSectionMedia(sectionId = libraryId, start = null, size = null, sort = sort)
            }
        }
        return when (res) {
            is NetworkResult.Success -> {
                val container = res.data.mediaContainer
                val initialMetadata = container.metadata.orEmpty().filter { !it.ratingKey.isNullOrBlank() }
                val totalSize = container.totalSize ?: initialMetadata.size
                if (initialMetadata.size >= totalSize) {
                    NetworkResult.Success(initialMetadata.map { it.toUnifiedMediaItem(libraryId) })
                } else {
                    val allMetadata = initialMetadata.toMutableList()
                    val batchSize = 500
                    while (allMetadata.size < totalSize) {
                        val nextBatchRes = client.getSectionMedia(
                            sectionId = libraryId,
                            start = allMetadata.size,
                            size = batchSize,
                            sort = sort,
                        )
                        if (nextBatchRes is NetworkResult.Success) {
                            val nextItems = nextBatchRes.data.mediaContainer.metadata.orEmpty()
                                .filter { !it.ratingKey.isNullOrBlank() }
                            if (nextItems.isEmpty()) break
                            allMetadata.addAll(nextItems)
                        } else {
                            break
                        }
                    }
                    NetworkResult.Success(allMetadata.map { it.toUnifiedMediaItem(libraryId) })
                }
            }
            is NetworkResult.Error -> res
            is NetworkResult.Loading -> res
        }
    }

    private fun isRecoverableNetworkError(error: NetworkResult.Error): Boolean {
        val msg = error.message ?: ""
        return msg.contains("UnknownHostException", ignoreCase = true) ||
            msg.contains("No address associated with hostname", ignoreCase = true) ||
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("Connection refused", ignoreCase = true) ||
            msg.contains("ConnectException", ignoreCase = true) ||
            PlexConnectionUtils.isDockerBridgeUrl(client.baseUrl)
    }

    suspend fun autoHealFromPlexTv(): Boolean {
        val token = integration.token.value
        if (token.isBlank()) return false
        val clientIdentifier = integration.clientIdentifier

        val res = client.httpClient.safeGet<List<PlexServerResource>>("https://plex.tv/api/v2/resources?includeHttps=1") {
            header("Accept", "application/json")
            header("X-Plex-Token", token)
            header("X-Plex-Client-Identifier", clientIdentifier)
            parameter("X-Plex-Token", token)
            parameter("X-Plex-Client-Identifier", clientIdentifier)
        }
        if (res !is NetworkResult.Success) return false

        val servers = res.data.filter { it.provides.contains("server") }
        val matchedServer = servers.firstOrNull { it.clientIdentifier == integration.serverId }
            ?: servers.firstOrNull() ?: return false

        val sorted = PlexConnectionUtils.sortConnections(matchedServer.connections)
        for (conn in sorted) {
            val isDocker = PlexConnectionUtils.isDockerBridgeIp(conn.address)
            if (isDocker) continue

            val candidateUrls = listOfNotNull(
                conn.uri,
                "http://${conn.address}:${conn.port}".takeIf { it != conn.uri },
            )
            for (url in candidateUrls) {
                client.overrideBaseUrl = url
                when (client.getSections()) {
                    is NetworkResult.Success -> {
                        onUrlUpdated?.invoke(url)
                        return true
                    }
                    else -> {}
                }
            }
        }
        return false
    }

    override suspend fun getItemDetail(itemId: String): NetworkResult<UnifiedMediaItem> = when (val res = client.getItemMetadata(itemId)) {
        is NetworkResult.Success -> {
            val first = res.data.mediaContainer.metadata.orEmpty().firstOrNull { !it.ratingKey.isNullOrBlank() }
            if (first != null) {
                NetworkResult.Success(first.toUnifiedMediaItem())
            } else {
                NetworkResult.Error(message = "Item not found")
            }
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun getContinueWatching(limit: Int): NetworkResult<List<UnifiedMediaItem>> = when (val res = client.getOnDeck()) {
        is NetworkResult.Success -> {
            val items = res.data.mediaContainer.metadata.orEmpty()
                .filter { !it.ratingKey.isNullOrBlank() }
                .take(limit)
                .map { it.toUnifiedMediaItem() }
            NetworkResult.Success(items)
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun search(query: String, limit: Int): NetworkResult<List<UnifiedMediaItem>> = when (val res = client.search(query)) {
        is NetworkResult.Success -> {
            val items = res.data.mediaContainer.metadata.orEmpty()
                .filter { !it.ratingKey.isNullOrBlank() }
                .take(limit)
                .map { it.toUnifiedMediaItem() }
            NetworkResult.Success(items)
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun getShowEpisodes(showId: String): NetworkResult<List<UnifiedMediaItem>> = when (val res = client.getShowEpisodes(showId)) {
        is NetworkResult.Success -> {
            val items = res.data.mediaContainer.metadata.orEmpty()
                .filter { !it.ratingKey.isNullOrBlank() }
                .map { it.toUnifiedMediaItem() }
            NetworkResult.Success(items)
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun markWatched(itemId: String, watched: Boolean): NetworkResult<Unit> = client.markWatched(itemId, watched)

    override suspend fun refreshItem(itemId: String): NetworkResult<Unit> = client.refreshItem(itemId)

    override fun buildImageUrl(imageKeyOrTag: String, itemId: String, maxWidth: Int): String {
        if (imageKeyOrTag.startsWith("http://") || imageKeyOrTag.startsWith("https://")) {
            return imageKeyOrTag
        }
        val cleanKey = if (imageKeyOrTag.startsWith("/")) imageKeyOrTag else "/$imageKeyOrTag"
        val tokenParam = "X-Plex-Token=${integration.token.value}"
        return "$baseUrl/photo/:/transcode?width=$maxWidth&height=$maxWidth&minSize=1&upscale=1&url=$cleanKey&$tokenParam"
    }

    override fun buildDeepLinkUrl(item: UnifiedMediaItem): String {
        val serverParam = integration.serverId?.let { "&server=$it" } ?: ""
        return "plex://preplay/?metadataKey=%2Flibrary%2Fmetadata%2F${item.id}$serverParam"
    }

    override fun buildWebUrl(item: UnifiedMediaItem): String? {
        val serverId = integration.serverId ?: return null
        return "https://app.plex.tv/desktop/#!/server/$serverId/details?key=%2Flibrary%2Fmetadata%2F${item.id}"
    }

    private fun PlexMetadataItem.toUnifiedMediaItem(libraryId: String? = null): UnifiedMediaItem {
        val firstMedia = media?.firstOrNull()
        val rKey = ratingKey ?: ""
        return UnifiedMediaItem(
            id = rKey,
            title = title ?: "",
            type = type?.let { MediaType.fromString(it) } ?: MediaType.UNKNOWN,
            year = year,
            summary = summary,
            posterUrl = thumb?.let { buildImageUrl(it, rKey) },
            backdropUrl = art?.let { buildImageUrl(it, rKey, maxWidth = 720) },
            tmdbId = extractTmdbId(),
            tvdbId = extractTvdbId(),
            imdbId = extractImdbId(),
            playbackPositionTicks = viewOffset?.times(10_000),
            durationTicks = duration?.times(10_000),
            played = (viewCount ?: 0) > 0,
            rating = rating,
            serverType = MediaServerType.Plex,
            serverId = integration.serverId,
            libraryId = libraryId,
            seasonNumber = parentIndex,
            episodeNumber = index,
            seriesTitle = grandparentTitle ?: parentTitle,
            lastPlayedDate = lastViewedAt?.times(1000),
            audioCodec = firstMedia?.audioCodec,
            videoCodec = firstMedia?.videoCodec,
            videoResolution = firstMedia?.videoResolution,
            container = firstMedia?.container,
            bitrate = firstMedia?.bitrate,
            audioChannels = firstMedia?.audioChannels,
            videoRange = when {
                firstMedia?.videoProfile?.contains("dovi", ignoreCase = true) == true ||
                    firstMedia?.videoProfile?.contains("dv", ignoreCase = true) == true -> "DOVI"
                firstMedia?.videoProfile?.contains("hdr10+", ignoreCase = true) == true -> "HDR10+"
                firstMedia?.videoProfile?.contains("hdr", ignoreCase = true) == true ||
                    firstMedia?.videoProfile?.contains("main 10", ignoreCase = true) == true -> "HDR"
                else -> null
            },
        )
    }
}
