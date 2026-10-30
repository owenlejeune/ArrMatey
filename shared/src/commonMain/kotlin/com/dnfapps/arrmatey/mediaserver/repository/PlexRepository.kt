package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.mediaserver.client.PlexClient
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.PlexMetadataItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.networking.NetworkResult

class PlexRepository(
    private val client: PlexClient,
) : MediaServerRepository {
    override val serverType: MediaServerType = MediaServerType.Plex
    override val integration: MediaServerIntegration = client.integration

    private val baseUrl: String
        get() = integration.getEffectiveBaseUrl().trimEnd('/')

    override suspend fun getLibraries(): NetworkResult<List<UnifiedMediaLibrary>> = when (val res = client.getSections()) {
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

    override suspend fun getLibraryContents(
        libraryId: String,
        page: Int,
        pageSize: Int,
        sort: String?,
    ): NetworkResult<List<UnifiedMediaItem>> = when (val res = client.getSectionMedia(sectionId = libraryId, start = page * pageSize, size = pageSize, sort = sort)) {
        is NetworkResult.Success -> {
            val items = res.data.mediaContainer.metadata.orEmpty()
                .filter { !it.ratingKey.isNullOrBlank() }
                .map { it.toUnifiedMediaItem(libraryId) }
            NetworkResult.Success(items)
        }
        is NetworkResult.Error -> res
        is NetworkResult.Loading -> res
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

    override suspend fun markWatched(itemId: String, watched: Boolean): NetworkResult<Unit> = client.markWatched(itemId, watched)

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
            backdropUrl = art?.let { buildImageUrl(it, rKey, maxWidth = 1280) },
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
        )
    }
}
