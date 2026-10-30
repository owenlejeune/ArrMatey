package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.mediaserver.client.EmbyClient
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserBaseItem
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.networking.NetworkResult

class EmbyRepository(
    private val client: EmbyClient,
) : MediaServerRepository {
    override val serverType: MediaServerType = MediaServerType.Emby
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
                    serverType = MediaServerType.Emby,
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
    ): NetworkResult<List<UnifiedMediaItem>> = when (
        val res = client.getItems(
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

    override suspend fun markWatched(itemId: String, watched: Boolean): NetworkResult<Unit> = client.markWatched(userId, itemId, watched)

    override fun buildImageUrl(imageKeyOrTag: String, itemId: String, maxWidth: Int): String {
        if (imageKeyOrTag.startsWith("http://") || imageKeyOrTag.startsWith("https://")) {
            return imageKeyOrTag
        }
        val tagParam = if (imageKeyOrTag.isNotBlank()) "&tag=$imageKeyOrTag" else ""
        return "$baseUrl/Items/$itemId/Images/Primary?maxWidth=$maxWidth$tagParam"
    }

    override fun buildDeepLinkUrl(item: UnifiedMediaItem): String {
        val serverParam = integration.serverId?.let { "&serverId=$it" } ?: ""
        return "emby://open?itemId=${item.id}$serverParam"
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
            backdropUrl = backdropTag?.let { "$baseUrl/Items/$id/Images/Backdrop/0?maxWidth=1280&tag=$it" },
            tmdbId = extractTmdbId(),
            tvdbId = extractTvdbId(),
            imdbId = extractImdbId(),
            playbackPositionTicks = userData?.playbackPositionTicks,
            durationTicks = runTimeTicks,
            played = userData?.played == true,
            rating = communityRating,
            serverType = MediaServerType.Emby,
            serverId = integration.serverId,
            libraryId = libraryId,
            seasonNumber = parentIndexNumber,
            episodeNumber = indexNumber,
            seriesTitle = seriesName,
            audioCodec = audioStream?.codec,
            videoCodec = videoStream?.codec,
            videoResolution = resolution,
        )
    }
}
