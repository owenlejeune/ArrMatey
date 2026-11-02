package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.mediaserver.client.JellyfinClient
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserBaseItem
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.networking.NetworkResult

class JellyfinRepository(
    private val client: JellyfinClient,
) : MediaServerRepository {
    override val serverType: MediaServerType = MediaServerType.Jellyfin
    override val integration: MediaServerIntegration = client.integration

    private val baseUrl: String
        get() = integration.getEffectiveBaseUrl().trimEnd('/')

    private val userId: String
        get() = integration.userId.orEmpty()

    override suspend fun getLibraries(): NetworkResult<List<UnifiedMediaLibrary>> = when (val res = client.getUserViews(userId)) {
        is NetworkResult.Success -> {
            val libs = res.data.items.map { item ->
                UnifiedMediaLibrary(
                    id = item.id,
                    title = item.name,
                    type = MediaType.fromString(item.collectionType ?: item.type),
                    serverType = MediaServerType.Jellyfin,
                )
            }
            NetworkResult.Success(libs)
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
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
        return when (
            val res = client.getLibraryItems(
                userId = userId,
                parentId = libraryId,
                startIndex = page * pageSize,
                limit = pageSize,
                sortBy = sort ?: "SortName",
            )
        ) {
            is NetworkResult.Success -> {
                val items = res.data.items.map { it.toUnifiedMediaItem(libraryId) }
                NetworkResult.Success(items)
            }
            is NetworkResult.Error -> res
            is NetworkResult.Loading -> res
        }
    }

    override suspend fun getAllLibraryContents(
        libraryId: String,
        sort: String?,
    ): NetworkResult<List<UnifiedMediaItem>> = when (
        val res = client.getLibraryItems(
            userId = userId,
            parentId = libraryId,
            startIndex = null,
            limit = null,
            sortBy = sort ?: "SortName",
        )
    ) {
        is NetworkResult.Success -> {
            val initialItems = res.data.items
            val totalCount = res.data.totalRecordCount
            if (initialItems.size >= totalCount) {
                NetworkResult.Success(initialItems.map { it.toUnifiedMediaItem(libraryId) })
            } else {
                val allItems = initialItems.toMutableList()
                val batchSize = 500
                while (allItems.size < totalCount) {
                    val nextBatchRes = client.getLibraryItems(
                        userId = userId,
                        parentId = libraryId,
                        startIndex = allItems.size,
                        limit = batchSize,
                        sortBy = sort ?: "SortName",
                    )
                    if (nextBatchRes is NetworkResult.Success) {
                        if (nextBatchRes.data.items.isEmpty()) break
                        allItems.addAll(nextBatchRes.data.items)
                    } else {
                        break
                    }
                }
                NetworkResult.Success(allItems.map { it.toUnifiedMediaItem(libraryId) })
            }
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun getItemDetail(itemId: String): NetworkResult<UnifiedMediaItem> = when (val res = client.getItemDetails(userId, itemId)) {
        is NetworkResult.Success -> NetworkResult.Success(res.data.toUnifiedMediaItem())
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun getContinueWatching(limit: Int): NetworkResult<List<UnifiedMediaItem>> = when (val res = client.getResumeItems(userId, limit)) {
        is NetworkResult.Success -> {
            val items = res.data.items.map { it.toUnifiedMediaItem() }
            NetworkResult.Success(items)
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun search(query: String, limit: Int): NetworkResult<List<UnifiedMediaItem>> = when (val res = client.search(userId, query, limit)) {
        is NetworkResult.Success -> {
            val items = res.data.items.map { it.toUnifiedMediaItem() }
            NetworkResult.Success(items)
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun getShowEpisodes(showId: String): NetworkResult<List<UnifiedMediaItem>> = when (val res = client.getShowEpisodes(userId, showId)) {
        is NetworkResult.Success -> {
            val items = res.data.items.map { it.toUnifiedMediaItem() }
            NetworkResult.Success(items)
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
    }

    override suspend fun markWatched(itemId: String, watched: Boolean): NetworkResult<Unit> = client.markWatched(userId, itemId, watched)

    override suspend fun refreshItem(itemId: String): NetworkResult<Unit> = client.refreshItem(itemId)

    override fun buildImageUrl(imageKeyOrTag: String, itemId: String, maxWidth: Int): String {
        if (imageKeyOrTag.startsWith("http://") || imageKeyOrTag.startsWith("https://")) {
            return imageKeyOrTag
        }
        val tagParam = if (imageKeyOrTag.isNotBlank()) "&tag=$imageKeyOrTag" else ""
        return "$baseUrl/Items/$itemId/Images/Primary?maxWidth=$maxWidth$tagParam"
    }

    override fun buildDeepLinkUrl(item: UnifiedMediaItem): String {
        val serverParam = integration.serverId?.let { "&serverId=$it" } ?: ""
        return "jellyfin://open?itemId=${item.id}$serverParam"
    }

    override fun buildWebUrl(item: UnifiedMediaItem): String? {
        val cleanBase = baseUrl.trimEnd('/')
        val serverParam = integration.serverId?.let { "&serverId=$it" } ?: ""
        return "$cleanBase/web/index.html#!/details?id=${item.id}$serverParam"
    }

    private fun MediaBrowserBaseItem.toUnifiedMediaItem(libraryId: String? = null): UnifiedMediaItem {
        val primaryTag = imageTags?.get("Primary")
        val backdropTag = backdropImageTags.firstOrNull() ?: imageTags?.get("Backdrop")
        val mediaSource = mediaSources.firstOrNull()
        val videoStream = mediaSource?.mediaStreams?.firstOrNull { it.type == "Video" }
        val audioStream = mediaSource?.mediaStreams?.firstOrNull { it.type == "Audio" }

        val resolution = if (videoStream?.width != null && videoStream.height != null) {
            "${videoStream.width}x${videoStream.height}"
        } else {
            null
        }

        return UnifiedMediaItem(
            id = id,
            title = name,
            type = MediaType.fromString(type),
            year = productionYear,
            summary = overview,
            posterUrl = primaryTag?.let { buildImageUrl(it, id) } ?: "$baseUrl/Items/$id/Images/Primary",
            backdropUrl = backdropTag?.let { "$baseUrl/Items/$id/Images/Backdrop/0?maxWidth=720&tag=$it" },
            tmdbId = extractTmdbId(),
            tvdbId = extractTvdbId(),
            imdbId = extractImdbId(),
            playbackPositionTicks = userData?.playbackPositionTicks,
            durationTicks = runTimeTicks,
            played = userData?.played == true,
            rating = communityRating,
            serverType = MediaServerType.Jellyfin,
            serverId = integration.serverId,
            libraryId = libraryId,
            seasonNumber = parentIndexNumber,
            episodeNumber = indexNumber,
            seriesTitle = seriesName,
            audioCodec = audioStream?.codec,
            videoCodec = videoStream?.codec,
            videoResolution = resolution,
            container = mediaSource?.container,
            bitrate = mediaSource?.bitrate,
            audioChannels = audioStream?.channels,
            videoRange = videoStream?.videoRange,
        )
    }
}
