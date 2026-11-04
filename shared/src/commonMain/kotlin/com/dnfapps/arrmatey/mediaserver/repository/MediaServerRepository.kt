package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import com.dnfapps.networking.NetworkResult

interface MediaServerRepository {
    val serverType: MediaServerType
    val integration: MediaServerIntegration

    suspend fun getLibraries(): NetworkResult<List<UnifiedMediaLibrary>>

    suspend fun getLibraryContents(
        libraryId: String,
        page: Int = 0,
        pageSize: Int = 50,
        sort: String? = null,
    ): NetworkResult<List<UnifiedMediaItem>>

    suspend fun getAllLibraryContents(
        libraryId: String,
        sort: String? = null,
    ): NetworkResult<List<UnifiedMediaItem>> = getLibraryContents(libraryId, page = 0, pageSize = -1, sort = sort)

    suspend fun getItemDetail(itemId: String): NetworkResult<UnifiedMediaItem>

    suspend fun getContinueWatching(limit: Int = 20): NetworkResult<List<UnifiedMediaItem>>

    suspend fun search(query: String, limit: Int = 30): NetworkResult<List<UnifiedMediaItem>>

    suspend fun getShowEpisodes(showId: String): NetworkResult<List<UnifiedMediaItem>>

    suspend fun markWatched(itemId: String, watched: Boolean): NetworkResult<Unit>

    suspend fun refreshItem(itemId: String): NetworkResult<Unit>

    fun buildImageUrl(imageKeyOrTag: String, itemId: String, maxWidth: Int = 400): String

    fun buildDeepLinkUrl(item: UnifiedMediaItem): String

    fun buildWebUrl(item: UnifiedMediaItem): String?
}
