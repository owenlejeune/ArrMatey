package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.mediaserver.model.MediaServerMediaDetails
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.seerr.state.WatchOption
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UnifiedMediaDetailsMediaServerHandler(
    private val scope: CoroutineScope,
    private val mediaServerManager: MediaServerManager,
) {
    private val _mediaServerDetails = MutableStateFlow<List<MediaServerMediaDetails>>(emptyList())
    val mediaServerDetails: StateFlow<List<MediaServerMediaDetails>> = _mediaServerDetails.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _watchOptions = MutableStateFlow<List<WatchOption>>(emptyList())
    val watchOptions: StateFlow<List<WatchOption>> = _watchOptions.asStateFlow()

    fun findMedia(
        tmdbId: Long?,
        tvdbId: Long?,
        title: String?,
        year: Int?,
        isShow: Boolean,
    ) {
        if (title.isNullOrBlank() && tmdbId == null && tvdbId == null) return
        scope.launch {
            _isSearching.value = true
            try {
                val repos = mediaServerManager.activeRepositories.value
                val results = mutableListOf<MediaServerMediaDetails>()
                val options = mutableListOf<WatchOption>()

                for (repo in repos) {
                    val searchResult = repo.search(title ?: "", limit = 15)
                    if (searchResult is NetworkResult.Success) {
                        val matchingItem = searchResult.data.firstOrNull { item ->
                            val tmdbMatch = tmdbId != null && item.tmdbId != null && tmdbId == item.tmdbId
                            val tvdbMatch = tvdbId != null && item.tvdbId != null && tvdbId == item.tvdbId
                            val titleMatch = !title.isNullOrBlank() && item.title.equals(title, ignoreCase = true)
                            val yearMatch = year == null || item.year == null || year == item.year
                            tmdbMatch || tvdbMatch || (titleMatch && yearMatch)
                        }

                        if (matchingItem != null) {
                            val deepLink = repo.buildDeepLinkUrl(matchingItem)
                            val webUrl = repo.buildWebUrl(matchingItem)
                            val provider = when (repo.serverType) {
                                MediaServerType.Plex -> MediaProvider.Plex
                                MediaServerType.Jellyfin -> MediaProvider.Jellyfin
                                MediaServerType.Emby -> MediaProvider.Emby
                            }
                            val label = when (repo.serverType) {
                                MediaServerType.Plex -> MR.strings.watch_on_plex
                                MediaServerType.Jellyfin -> MR.strings.watch_on_jellyfin
                                MediaServerType.Emby -> MR.strings.watch_on_emby
                            }

                            options.add(
                                WatchOption(
                                    provider = provider,
                                    serverName = repo.integration.label,
                                    url = deepLink,
                                    webFallbackUrl = webUrl,
                                    label = label,
                                ),
                            )

                            var epWatchStatus = emptyMap<Pair<Int, Int>, Boolean>()
                            var epDeepLinks = emptyMap<Pair<Int, Int>, String>()
                            var epItemIds = emptyMap<Pair<Int, Int>, String>()
                            var episodesWatched = 0
                            var totalEpisodes = 0

                            if (isShow) {
                                val episodesRes = repo.getShowEpisodes(matchingItem.id)
                                if (episodesRes is NetworkResult.Success) {
                                    val statusMap = mutableMapOf<Pair<Int, Int>, Boolean>()
                                    val linksMap = mutableMapOf<Pair<Int, Int>, String>()
                                    val itemIdsMap = mutableMapOf<Pair<Int, Int>, String>()
                                    totalEpisodes = episodesRes.data.size
                                    for (ep in episodesRes.data) {
                                        val sNum = ep.seasonNumber ?: 1
                                        val eNum = ep.episodeNumber ?: 0
                                        val key = sNum to eNum
                                        statusMap[key] = ep.played
                                        linksMap[key] = repo.buildDeepLinkUrl(ep)
                                        itemIdsMap[key] = ep.id
                                        if (ep.played) episodesWatched++
                                    }
                                    epWatchStatus = statusMap
                                    epDeepLinks = linksMap
                                    epItemIds = itemIdsMap
                                }
                            }

                            results.add(
                                MediaServerMediaDetails(
                                    serverType = repo.serverType,
                                    serverId = repo.integration.serverId,
                                    serverLabel = repo.integration.label,
                                    itemId = matchingItem.id,
                                    title = matchingItem.title,
                                    deepLinkUrl = deepLink,
                                    webFallbackUrl = webUrl,
                                    played = matchingItem.played,
                                    playbackPositionTicks = matchingItem.playbackPositionTicks,
                                    durationTicks = matchingItem.durationTicks,
                                    videoCodec = matchingItem.videoCodec,
                                    audioCodec = matchingItem.audioCodec,
                                    videoResolution = matchingItem.videoResolution,
                                    container = matchingItem.container,
                                    bitrate = matchingItem.bitrate,
                                    episodesWatchedCount = if (isShow) episodesWatched else null,
                                    totalEpisodesCount = if (isShow) totalEpisodes else null,
                                    episodeWatchStatus = epWatchStatus,
                                    episodeDeepLinks = epDeepLinks,
                                    episodeItemIds = epItemIds,
                                ),
                            )
                        }
                    }
                }

                _mediaServerDetails.value = results
                _watchOptions.value = options
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun findEpisode(
        tmdbId: Long?,
        tvdbId: Long?,
        title: String?,
        year: Int?,
        seasonNumber: Int,
        episodeNumber: Int,
    ) {
        if (title.isNullOrBlank() && tmdbId == null && tvdbId == null) return
        scope.launch {
            _isSearching.value = true
            try {
                val repos = mediaServerManager.activeRepositories.value
                val results = mutableListOf<MediaServerMediaDetails>()
                val options = mutableListOf<WatchOption>()

                for (repo in repos) {
                    val searchResult = repo.search(title ?: "", limit = 15)
                    if (searchResult is NetworkResult.Success) {
                        val matchingShow = searchResult.data.firstOrNull { item ->
                            val tmdbMatch = tmdbId != null && item.tmdbId != null && tmdbId == item.tmdbId
                            val tvdbMatch = tvdbId != null && item.tvdbId != null && tvdbId == item.tvdbId
                            val titleMatch = !title.isNullOrBlank() && item.title.equals(title, ignoreCase = true)
                            val yearMatch = year == null || item.year == null || year == item.year
                            tmdbMatch || tvdbMatch || (titleMatch && yearMatch)
                        }

                        if (matchingShow != null) {
                            val episodesRes = repo.getShowEpisodes(matchingShow.id)
                            if (episodesRes is NetworkResult.Success) {
                                val matchingEp = episodesRes.data.firstOrNull { ep ->
                                    val sNum = ep.seasonNumber ?: 1
                                    val eNum = ep.episodeNumber ?: 0
                                    sNum == seasonNumber && eNum == episodeNumber
                                }

                                if (matchingEp != null) {
                                    val deepLink = repo.buildDeepLinkUrl(matchingEp)
                                    val webUrl = repo.buildWebUrl(matchingEp)
                                    val provider = when (repo.serverType) {
                                        MediaServerType.Plex -> MediaProvider.Plex
                                        MediaServerType.Jellyfin -> MediaProvider.Jellyfin
                                        MediaServerType.Emby -> MediaProvider.Emby
                                    }
                                    val label = when (repo.serverType) {
                                        MediaServerType.Plex -> MR.strings.watch_on_plex
                                        MediaServerType.Jellyfin -> MR.strings.watch_on_jellyfin
                                        MediaServerType.Emby -> MR.strings.watch_on_emby
                                    }

                                    options.add(
                                        WatchOption(
                                            provider = provider,
                                            serverName = repo.integration.label,
                                            url = deepLink,
                                            webFallbackUrl = webUrl,
                                            label = label,
                                        ),
                                    )

                                    results.add(
                                        MediaServerMediaDetails(
                                            serverType = repo.serverType,
                                            serverId = repo.integration.serverId,
                                            serverLabel = repo.integration.label,
                                            itemId = matchingEp.id,
                                            title = matchingEp.title,
                                            deepLinkUrl = deepLink,
                                            webFallbackUrl = webUrl,
                                            played = matchingEp.played,
                                            playbackPositionTicks = matchingEp.playbackPositionTicks,
                                            durationTicks = matchingEp.durationTicks,
                                            videoCodec = matchingEp.videoCodec,
                                            audioCodec = matchingEp.audioCodec,
                                            videoResolution = matchingEp.videoResolution,
                                            container = matchingEp.container,
                                            bitrate = matchingEp.bitrate,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                _mediaServerDetails.value = results
                _watchOptions.value = options
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun markWatched(serverType: MediaServerType, itemId: String, watched: Boolean) {
        scope.launch {
            val repo = mediaServerManager.getRepository(serverType) ?: return@launch
            repo.markWatched(itemId, watched)
            _mediaServerDetails.update { list ->
                list.map { detail ->
                    if (detail.serverType == serverType) {
                        if (detail.itemId == itemId) {
                            detail.copy(played = watched, playbackPositionTicks = if (watched) detail.durationTicks else 0L)
                        } else if (detail.episodeItemIds.containsValue(itemId)) {
                            val updatedStatus = detail.episodeWatchStatus.toMutableMap()
                            val key = detail.episodeItemIds.entries.firstOrNull { it.value == itemId }?.key
                            if (key != null) {
                                updatedStatus[key] = watched
                            }
                            val updatedWatchedCount = updatedStatus.values.count { it }
                            detail.copy(
                                episodeWatchStatus = updatedStatus,
                                episodesWatchedCount = updatedWatchedCount,
                            )
                        } else {
                            detail
                        }
                    } else {
                        detail
                    }
                }
            }
        }
    }

    fun markEpisodeWatched(
        serverType: MediaServerType,
        seasonNumber: Int,
        episodeNumber: Int,
        watched: Boolean,
    ) {
        val detail = _mediaServerDetails.value.firstOrNull { it.serverType == serverType } ?: return
        val itemId = detail.episodeItemIds[seasonNumber to episodeNumber] ?: return
        markWatched(serverType, itemId, watched)
    }
}
