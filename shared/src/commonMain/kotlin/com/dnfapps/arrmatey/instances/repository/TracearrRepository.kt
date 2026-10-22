package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.monitoring.model.PlaybackHistoryItem
import com.dnfapps.arrmatey.monitoring.model.PlaybackMediaType
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamDecision
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.arrmatey.monitoring.model.PlaybackSummaryStats
import com.dnfapps.arrmatey.tracearr.api.client.TracearrClient
import com.dnfapps.arrmatey.tracearr.api.model.TracearrActivityResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaDetails
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaStatsWindow
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaWatchers
import com.dnfapps.arrmatey.tracearr.api.model.TracearrPeriod
import com.dnfapps.arrmatey.tracearr.api.model.TracearrServer
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamDecision
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamsResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUsersResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrVersion
import com.dnfapps.arrmatey.tracearr.api.model.TracearrViolationsResponse
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TracearrRepository(
    override val instance: Instance,
    httpClient: HttpClient,
) : PlaybackMonitoringRepository {
    private val tracearrClient = TracearrClient(instance, httpClient)

    private val _todayStats = MutableStateFlow<TracearrTodayStats?>(null)
    val todayStats: StateFlow<TracearrTodayStats?> = _todayStats.asStateFlow()

    private val _activeStreams = MutableStateFlow<List<TracearrStreamSession>>(emptyList())
    val activeStreams: StateFlow<List<TracearrStreamSession>> = _activeStreams.asStateFlow()

    private val _streamSessions = MutableStateFlow<List<PlaybackStreamSession>>(emptyList())
    override val streamSessions: StateFlow<List<PlaybackStreamSession>> = _streamSessions.asStateFlow()

    private val _summaryStats = MutableStateFlow<PlaybackSummaryStats?>(null)
    override val summaryStats: StateFlow<PlaybackSummaryStats?> = _summaryStats.asStateFlow()

    private val _servers = MutableStateFlow<List<TracearrServer>>(emptyList())
    val servers: StateFlow<List<TracearrServer>> = _servers.asStateFlow()

    private val _version = MutableStateFlow<TracearrVersion?>(null)
    val version: StateFlow<TracearrVersion?> = _version.asStateFlow()

    suspend fun getVersion(): NetworkResult<TracearrVersion> = when (val result = tracearrClient.getVersion()) {
        is NetworkResult.Success -> {
            _version.value = result.data
            NetworkResult.Success(result.data)
        }
        is NetworkResult.Error -> NetworkResult.Error(code = result.code, message = result.message, cause = result.cause, errorType = result.errorType)
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    private val mediaDetailsCache = mutableMapOf<String, TracearrMediaDetails>()
    private val cacheMutex = Mutex()

    suspend fun getServers(): NetworkResult<List<TracearrServer>> {
        if (_servers.value.isNotEmpty()) {
            return NetworkResult.Success(_servers.value)
        }
        return refreshServers()
    }

    suspend fun refreshServers(): NetworkResult<List<TracearrServer>> = when (val result = tracearrClient.getServers()) {
        is NetworkResult.Success -> {
            _servers.value = result.data.data
            NetworkResult.Success(result.data.data)
        }
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun getServerMap(): Map<String, TracearrServer> {
        if (_servers.value.isEmpty()) {
            refreshServers()
        }
        return _servers.value.mapNotNull { s -> s.serverId?.let { it to s } }.toMap()
    }

    override suspend fun testConnection(): NetworkResult<Unit> = tracearrClient.testConnection()

    suspend fun refreshStats(): NetworkResult<TracearrTodayStats> = getTodayStats()

    suspend fun refreshStreams(): NetworkResult<TracearrStreamsResponse> = getPublicStreams()

    suspend fun getPublicStreams(): NetworkResult<TracearrStreamsResponse> {
        val baseUrl = instance.getEffectiveBaseUrl()
        return when (val streamsResult = tracearrClient.getPublicStreams()) {
            is NetworkResult.Success -> {
                val serverMap = getServerMap()
                val refs =
                    streamsResult.data.data.mapNotNull { session ->
                        (session.showMediaId ?: session.mediaId)?.takeIf { it.isNotBlank() }
                    }
                val detailsMap = fetchMediaDetailsForRefs(refs)

                val updatedSessions =
                    streamsResult.data.data.map { session ->
                        val ref = (session.showMediaId ?: session.mediaId)?.takeIf { it.isNotBlank() }
                        val matchingServer = session.serverId?.let { serverMap[it] }
                        val resolvedServer = session.server ?: matchingServer
                        val resolvedServerName = session.serverName ?: matchingServer?.serverName
                        val resolvedServerType = session.serverType ?: matchingServer?.serverType
                        session.rebuildWithInstanceBaseUrl(baseUrl).copy(
                            mediaDetails = ref?.let { detailsMap[it] },
                            server = resolvedServer,
                            serverName = resolvedServerName,
                            serverType = resolvedServerType,
                        )
                    }

                _activeStreams.value = updatedSessions
                _streamSessions.value = updatedSessions.map { it.toPlaybackStreamSession(instance) }
                updateSummaryStats()
                NetworkResult.Success(streamsResult.data.copy(data = updatedSessions))
            }
            is NetworkResult.Error -> {
                _streamSessions.value = emptyList()
                streamsResult
            }
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    override suspend fun refreshPlaybackStreams(): NetworkResult<List<PlaybackStreamSession>> = when (val result = getPublicStreams()) {
        is NetworkResult.Success -> NetworkResult.Success(_streamSessions.value)
        is NetworkResult.Error -> NetworkResult.Error(code = result.code, message = result.message, cause = result.cause, errorType = result.errorType)
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun getTodayStats(): NetworkResult<TracearrTodayStats> = tracearrClient
        .getTodayStats()
        .onSuccess {
            _todayStats.value = it
            updateSummaryStats()
        }.onError { _, _, _ ->
            _todayStats.value = null
        }

    override suspend fun refreshPlaybackStats(): NetworkResult<PlaybackSummaryStats> {
        val statsRes = getTodayStats()
        val summary = _summaryStats.value ?: PlaybackSummaryStats()
        return when (statsRes) {
            is NetworkResult.Success -> NetworkResult.Success(summary)
            is NetworkResult.Error -> NetworkResult.Error(code = statsRes.code, message = statsRes.message)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    override suspend fun terminatePlaybackStream(
        sessionId: String,
        message: String?,
    ): NetworkResult<Unit> = NetworkResult.Error(code = 501, message = "Termination not supported on Tracearr")

    suspend fun getHistory(
        cursor: String? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrHistoryResponse> {
        val baseUrl = instance.getEffectiveBaseUrl()
        return when (val historyResult = tracearrClient.getHistory(cursor = cursor, pageSize = pageSize)) {
            is NetworkResult.Success -> {
                val serverMap = getServerMap()
                val refs =
                    historyResult.data.data.mapNotNull { item ->
                        (item.showMediaId ?: item.mediaId)?.takeIf { it.isNotBlank() }
                    }
                val detailsMap = fetchMediaDetailsForRefs(refs)

                val updatedItems =
                    historyResult.data.data.map { item ->
                        val ref = (item.showMediaId ?: item.mediaId)?.takeIf { it.isNotBlank() }
                        val matchingServer = item.serverId?.let { serverMap[it] }
                        val resolvedServerName = item.serverName ?: matchingServer?.serverName
                        val resolvedServerType = item.serverType ?: matchingServer?.serverType
                        item.rebuildWithInstanceBaseUrl(baseUrl).copy(
                            mediaDetails = ref?.let { detailsMap[it] },
                            serverName = resolvedServerName,
                            serverType = resolvedServerType,
                        )
                    }

                NetworkResult.Success(historyResult.data.copy(data = updatedItems))
            }
            is NetworkResult.Error -> historyResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    private var lastPlaybackHistoryCursor: String? = null

    override suspend fun getPlaybackHistory(
        pageOrStart: Int,
        pageSize: Int,
    ): NetworkResult<List<PlaybackHistoryItem>> {
        val cursor = if (pageOrStart == 0) null else lastPlaybackHistoryCursor
        if (pageOrStart > 0 && cursor.isNullOrBlank()) {
            return NetworkResult.Success(emptyList())
        }
        return when (val result = getHistory(cursor = cursor, pageSize = pageSize)) {
            is NetworkResult.Success -> {
                lastPlaybackHistoryCursor = result.data.meta?.nextCursor
                val items = result.data.data.map { it.toPlaybackHistoryItem(instance) }
                NetworkResult.Success(items)
            }
            is NetworkResult.Error -> NetworkResult.Error(code = result.code, message = result.message)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    private val uuidRegex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    private fun isUuid(ref: String): Boolean = uuidRegex.matches(ref)

    suspend fun getUsers(
        cursor: String? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrUsersResponse> = when (val result = tracearrClient.getUsers(cursor = cursor, pageSize = pageSize)) {
        is NetworkResult.Success -> {
            val serverMap = getServerMap()
            val enrichedUsers = result.data.data.map { user ->
                val enrichedAccounts = user.accounts.map { acc ->
                    val matchingServer = acc.serverId?.let { serverMap[it] }
                    acc.copy(
                        serverName = acc.serverName ?: matchingServer?.serverName,
                        serverType = acc.serverType ?: matchingServer?.serverType,
                    )
                }
                user.copy(accounts = enrichedAccounts)
            }
            NetworkResult.Success(result.data.copy(data = enrichedUsers))
        }
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun getViolations(
        page: Int? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrViolationsResponse> = when (val result = tracearrClient.getViolations(page = page, pageSize = pageSize)) {
        is NetworkResult.Success -> {
            val serverMap = getServerMap()
            val enrichedViolations = result.data.data.map { violation ->
                val matchingServer = violation.serverId?.let { serverMap[it] }
                val resolvedServerName = violation.serverName ?: matchingServer?.serverName
                violation.copy(serverName = resolvedServerName)
            }
            NetworkResult.Success(result.data.copy(data = enrichedViolations))
        }
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun getActivity(period: TracearrPeriod = TracearrPeriod.Month): NetworkResult<TracearrActivityResponse> = when (val result = tracearrClient.getActivity(period)) {
        is NetworkResult.Success -> {
            val serverMap = getServerMap()
            val enrichedPlays = result.data.plays.map { play ->
                val resolvedName = play.serverId?.let { serverMap[it]?.serverName }
                play.copy(serverName = resolvedName ?: play.serverName)
            }
            NetworkResult.Success(result.data.copy(plays = enrichedPlays))
        }
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun getUserDetails(ref: String): NetworkResult<TracearrUserDetail> = resolveUserDetail(ref)

    suspend fun resolveUserDetail(ref: String): NetworkResult<TracearrUserDetail> {
        val serverMap = getServerMap()
        fun enrichUserDetail(detail: TracearrUserDetail): TracearrUserDetail {
            val enrichedAccounts = detail.accounts.map { acc ->
                val matchingServer = acc.serverId?.let { serverMap[it] }
                acc.copy(
                    serverName = acc.serverName ?: matchingServer?.serverName,
                    serverType = acc.serverType ?: matchingServer?.serverType,
                )
            }
            return detail.copy(accounts = enrichedAccounts)
        }

        if (isUuid(ref)) {
            val directResult = tracearrClient.getUserDetails(ref)
            if (directResult is NetworkResult.Success) {
                return NetworkResult.Success(enrichUserDetail(directResult.data))
            }
        }

        return when (val usersResult = tracearrClient.getUsers(pageSize = 100)) {
            is NetworkResult.Success -> {
                val matchingUser =
                    usersResult.data.data.firstOrNull { u ->
                        u.id.equals(ref, ignoreCase = true) ||
                            u.username?.equals(ref, ignoreCase = true) == true ||
                            u.accounts.any { acc ->
                                acc.username?.equals(ref, ignoreCase = true) == true ||
                                    acc.serverUserId == ref ||
                                    acc.externalUserId == ref
                            }
                    }

                if (matchingUser != null) {
                    val detailsResult = tracearrClient.getUserDetails(matchingUser.id)
                    if (detailsResult is NetworkResult.Success) {
                        NetworkResult.Success(enrichUserDetail(detailsResult.data))
                    } else {
                        NetworkResult.Success(enrichUserDetail(matchingUser))
                    }
                } else {
                    NetworkResult.Error(code = 404, message = "User not found")
                }
            }
            is NetworkResult.Error -> usersResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun getUserStats(ref: String): NetworkResult<TracearrUserStats> = tracearrClient.getUserStats(ref)

    suspend fun getUserHistory(
        ref: String,
        cursor: String? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrHistoryResponse> {
        val baseUrl = instance.getEffectiveBaseUrl()
        return when (val historyResult = tracearrClient.getUserHistory(ref = ref, cursor = cursor, pageSize = pageSize)) {
            is NetworkResult.Success -> {
                val serverMap = getServerMap()
                val refs =
                    historyResult.data.data.mapNotNull { item ->
                        (item.showMediaId ?: item.mediaId)?.takeIf { it.isNotBlank() }
                    }
                val detailsMap = fetchMediaDetailsForRefs(refs)

                val updatedItems =
                    historyResult.data.data.map { item ->
                        val itemRef = (item.showMediaId ?: item.mediaId)?.takeIf { it.isNotBlank() }
                        val matchingServer = item.serverId?.let { serverMap[it] }
                        val resolvedServerName = item.serverName ?: matchingServer?.serverName
                        val resolvedServerType = item.serverType ?: matchingServer?.serverType
                        item.rebuildWithInstanceBaseUrl(baseUrl).copy(
                            mediaDetails = itemRef?.let { detailsMap[it] },
                            serverName = resolvedServerName,
                            serverType = resolvedServerType,
                        )
                    }

                NetworkResult.Success(historyResult.data.copy(data = updatedItems))
            }
            is NetworkResult.Error -> historyResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun getMediaWatchers(ref: String): NetworkResult<TracearrMediaWatchers> = tracearrClient.getMediaWatchers(ref)

    suspend fun getMediaStats(ref: String): NetworkResult<TracearrMediaStats> = when (val result = tracearrClient.getMediaStats(ref)) {
        is NetworkResult.Success -> {
            val serverMap = getServerMap()
            fun enrichWindow(window: TracearrMediaStatsWindow?): TracearrMediaStatsWindow? {
                if (window == null) return null
                val enriched = window.perServer.map { stat ->
                    val matchingServer = stat.serverId?.let { serverMap[it] }
                    stat.copy(serverName = stat.serverName ?: matchingServer?.serverName)
                }
                return window.copy(perServer = enriched)
            }
            val windows = result.data.windows?.let { w ->
                w.copy(
                    allTime = enrichWindow(w.allTime),
                    last30 = enrichWindow(w.last30),
                    last7 = enrichWindow(w.last7),
                )
            }
            NetworkResult.Success(result.data.copy(windows = windows))
        }
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun getMediaHistory(
        ref: String,
        cursor: String? = null,
        pageSize: Int? = null,
    ): NetworkResult<TracearrHistoryResponse> {
        val baseUrl = instance.getEffectiveBaseUrl()
        return when (val historyResult = tracearrClient.getMediaHistory(ref = ref, cursor = cursor, pageSize = pageSize)) {
            is NetworkResult.Success -> {
                val serverMap = getServerMap()
                val refs =
                    historyResult.data.data.mapNotNull { item ->
                        (item.showMediaId ?: item.mediaId)?.takeIf { it.isNotBlank() }
                    }
                val detailsMap = fetchMediaDetailsForRefs(refs)

                val updatedItems =
                    historyResult.data.data.map { item ->
                        val itemRef = (item.showMediaId ?: item.mediaId)?.takeIf { it.isNotBlank() }
                        val matchingServer = item.serverId?.let { serverMap[it] }
                        val resolvedServerName = item.serverName ?: matchingServer?.serverName
                        val resolvedServerType = item.serverType ?: matchingServer?.serverType
                        item.rebuildWithInstanceBaseUrl(baseUrl).copy(
                            mediaDetails = itemRef?.let { detailsMap[it] },
                            serverName = resolvedServerName,
                            serverType = resolvedServerType,
                        )
                    }

                NetworkResult.Success(historyResult.data.copy(data = updatedItems))
            }
            is NetworkResult.Error -> historyResult
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    private suspend fun fetchMediaDetailsForRefs(refs: List<String>): Map<String, TracearrMediaDetails> {
        val missingRefs =
            cacheMutex.withLock {
                refs.filter { it.isNotBlank() && !mediaDetailsCache.containsKey(it) }.distinct()
            }

        if (missingRefs.isNotEmpty()) {
            val fetchedResults =
                coroutineScope {
                    missingRefs
                        .map { ref ->
                            async {
                                var details: TracearrMediaDetails? = null
                                tracearrClient.getMedia(ref).onSuccess {
                                    details = it
                                }
                                details?.let { ref to it }
                            }
                        }.awaitAll()
                        .filterNotNull()
                }

            if (fetchedResults.isNotEmpty()) {
                cacheMutex.withLock {
                    for ((ref, details) in fetchedResults) {
                        mediaDetailsCache[ref] = details
                    }
                }
            }
        }

        return cacheMutex.withLock {
            refs
                .mapNotNull { ref ->
                    mediaDetailsCache[ref]?.let { ref to it }
                }.toMap()
        }
    }

    private fun updateSummaryStats() {
        val today = _todayStats.value
        val streams = _activeStreams.value
        val transcodeCount = streams.count { it.isTranscode == true }
        val directPlayCount = streams.count { it.videoDecision == TracearrStreamDecision.DirectPlay }
        _summaryStats.value =
            PlaybackSummaryStats(
                activeStreamsCount = streams.size,
                transcodeCount = transcodeCount,
                directPlayCount = directPlayCount,
                totalBandwidthKbps = streams.mapNotNull { it.bitrate?.toLong() }.sum(),
                todayTotalPlays = today?.todayPlays?.toLong() ?: 0L,
                todayTotalDurationSeconds = (today?.watchTimeHours?.let { it * 3600 } ?: 0f).toLong(),
                activeUsersCount = today?.activeUsersToday ?: streams.mapNotNull { it.user?.id }.distinct().size,
            )
    }
}

private fun TracearrStreamSession.toPlaybackStreamSession(instance: Instance): PlaybackStreamSession {
    val mediaTypeEnum =
        when (mediaType) {
            TracearrMediaType.Movie -> PlaybackMediaType.Movie
            TracearrMediaType.Episode -> PlaybackMediaType.Episode
            TracearrMediaType.Track -> PlaybackMediaType.Track
            else -> PlaybackMediaType.Other
        }
    val decision =
        when (videoDecision ?: audioDecision) {
            TracearrStreamDecision.DirectPlay -> PlaybackStreamDecision.DirectPlay
            TracearrStreamDecision.Copy -> PlaybackStreamDecision.DirectStream
            TracearrStreamDecision.Transcode -> PlaybackStreamDecision.Transcode
            else -> PlaybackStreamDecision.Unknown
        }
    val calcProgress =
        if (totalDurationMs != null && totalDurationMs > 0 && progressMs != null) {
            (progressMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    val subtitleText =
        when {
            mediaType == TracearrMediaType.Episode ||
                (seasonNumber != null && episodeNumber != null) -> {
                val seasonStr = seasonNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                val episodeStr = episodeNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                "S$seasonStr E$episodeStr · ${mediaTitle ?: ""}"
            }
            mediaType == TracearrMediaType.Movie -> {
                listOfNotNull(year?.toString(), mediaTitle).filter { it.isNotBlank() }.joinToString(" · ")
            }
            else -> {
                listOfNotNull(artistName, albumName, mediaTitle)
                    .joinToString(" · ")
            }
        }
    return PlaybackStreamSession(
        sessionId = id,
        sessionKey = sessionKey,
        instanceId = instance.id,
        instanceLabel = instance.label,
        instanceType = instance.type,
        serverName = effectiveServerName,
        serverType = server?.type ?: serverType,
        ratingKey = mediaId,
        tmdbId = mediaDetails?.tmdbId ?: tmdbId,
        mediaType = mediaTypeEnum,
        title = mediaTitle ?: showTitle ?: "",
        parentTitle = null,
        grandparentTitle = grandparentTitle ?: showTitle,
        displayTitle = mediaTitle ?: showTitle ?: "",
        subtitle = subtitleText,
        year = year?.toString(),
        username = effectiveUsername,
        userDisplayName = user?.identityName ?: effectiveUsername,
        userAvatarUrl = effectiveUserAvatar,
        player = player ?: playerName,
        device = device ?: deviceId,
        platform = platform,
        ipAddress = ipAddress,
        location = geoCountry,
        progressPercent = calcProgress,
        progressMs = progressMs,
        totalDurationMs = totalDurationMs ?: durationMs,
        state = state,
        streamDecision = decision,
        isTranscoding = isTranscode == true,
        videoCodec = streamVideoCodec ?: sourceVideoCodec,
        videoResolution = resolution,
        audioCodec = streamAudioCodec ?: sourceAudioCodec,
        bandwidthKbps = bitrate?.toLong(),
        posterUrl = posterUrl ?: thumbPath,
        backdropUrl = null,
        canTerminate = canTerminate == true,
    )
}

private fun TracearrHistoryItem.toPlaybackHistoryItem(instance: Instance): PlaybackHistoryItem {
    val mediaTypeEnum =
        when (mediaType) {
            TracearrMediaType.Movie -> PlaybackMediaType.Movie
            TracearrMediaType.Episode -> PlaybackMediaType.Episode
            TracearrMediaType.Track -> PlaybackMediaType.Track
            else -> PlaybackMediaType.Other
        }
    val decision =
        when (videoDecision ?: audioDecision) {
            TracearrStreamDecision.DirectPlay -> PlaybackStreamDecision.DirectPlay
            TracearrStreamDecision.Copy -> PlaybackStreamDecision.DirectStream
            TracearrStreamDecision.Transcode -> PlaybackStreamDecision.Transcode
            else -> PlaybackStreamDecision.Unknown
        }
    val subtitleText =
        when {
            mediaType == TracearrMediaType.Episode ||
                (seasonNumber != null && episodeNumber != null) -> {
                val s = seasonNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                val e = episodeNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                "S$s E$e · ${mediaTitle ?: ""}"
            }
            mediaType == TracearrMediaType.Movie -> {
                listOfNotNull(year?.toString(), mediaTitle).filter { it.isNotBlank() }.joinToString(" · ")
            }
            else ->
                listOfNotNull(artistName, albumName, mediaTitle).filter { it.isNotBlank() }.joinToString(" · ")
        }
    val displayTitleText = grandparentTitle ?: showTitle ?: mediaTitle ?: ""
    return PlaybackHistoryItem(
        id = id,
        instanceId = instance.id,
        instanceType = instance.type,
        instanceLabel = instance.label,
        dateEpochSeconds = startedAt?.epochSeconds ?: stoppedAt?.epochSeconds ?: 0L,
        username = effectiveUsername,
        userDisplayName = user?.identityName ?: effectiveUsername,
        userAvatarUrl = effectiveUserAvatar,
        title = mediaTitle ?: showTitle ?: "",
        displayTitle = displayTitleText,
        mediaType = mediaTypeEnum,
        tmdbId = mediaDetails?.tmdbId ?: tmdbId,
        ratingKey = mediaId,
        player = player,
        platform = platform,
        ipAddress = null,
        durationSeconds = durationMs?.let { it / 1000L },
        percentComplete = percentComplete?.toInt(),
        streamDecision = decision,
        posterUrl = posterUrl ?: thumbPath,
        serverName = effectiveServerName.ifEmpty { instance.label },
        serverType = serverType,
        subtitle = subtitleText,
        resolution = resolution,
        watched = watched,
    )
}
