package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.monitoring.model.PlaybackHistoryItem
import com.dnfapps.arrmatey.monitoring.model.PlaybackMediaType
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamDecision
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.arrmatey.monitoring.model.PlaybackSummaryStats
import com.dnfapps.arrmatey.tautulli.api.client.TautulliClient
import com.dnfapps.arrmatey.tautulli.api.model.TautulliActivity
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHistoryItem
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHistoryResponse
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHomeStatSection
import com.dnfapps.arrmatey.tautulli.api.model.TautulliLibrary
import com.dnfapps.arrmatey.tautulli.api.model.TautulliMediaType
import com.dnfapps.arrmatey.tautulli.api.model.TautulliMetadata
import com.dnfapps.arrmatey.tautulli.api.model.TautulliServerStatus
import com.dnfapps.arrmatey.tautulli.api.model.TautulliSession
import com.dnfapps.arrmatey.tautulli.api.model.TautulliStatsType
import com.dnfapps.arrmatey.tautulli.api.model.TautulliStreamDecision
import com.dnfapps.arrmatey.tautulli.api.model.TautulliUser
import com.dnfapps.arrmatey.tautulli.api.model.TautulliUserWatchTime
import com.dnfapps.arrmatey.tracearr.api.model.TracearrServerType
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import io.ktor.client.HttpClient
import io.ktor.http.encodeURLQueryComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TautulliRepository(
    override val instance: Instance,
    httpClient: HttpClient,
) : PlaybackMonitoringRepository {
    private val tautulliClient = TautulliClient(instance, httpClient)

    private val _activity = MutableStateFlow<TautulliActivity?>(null)
    val activity: StateFlow<TautulliActivity?> = _activity.asStateFlow()

    private val _activeStreams = MutableStateFlow<List<TautulliSession>>(emptyList())
    val activeStreams: StateFlow<List<TautulliSession>> = _activeStreams.asStateFlow()

    private val _streamSessions = MutableStateFlow<List<PlaybackStreamSession>>(emptyList())
    override val streamSessions: StateFlow<List<PlaybackStreamSession>> = _streamSessions.asStateFlow()

    private val _summaryStats = MutableStateFlow<PlaybackSummaryStats?>(null)
    override val summaryStats: StateFlow<PlaybackSummaryStats?> = _summaryStats.asStateFlow()

    private val _homeStats = MutableStateFlow<List<TautulliHomeStatSection>>(emptyList())
    val homeStats: StateFlow<List<TautulliHomeStatSection>> = _homeStats.asStateFlow()

    private val _serverStatus = MutableStateFlow<TautulliServerStatus?>(null)
    val serverStatus: StateFlow<TautulliServerStatus?> = _serverStatus.asStateFlow()

    fun resolveServerName(vararg candidates: String?): String {
        for (candidate in candidates) {
            val trimmed = candidate?.trim()
            if (!trimmed.isNullOrEmpty()) return trimmed
        }
        val pms = _serverStatus.value?.pmsName?.trim()
        if (!pms.isNullOrEmpty()) return pms
        val label = instance.label.trim()
        if (label.isNotEmpty() && !label.equals("tautulli", ignoreCase = true)) return label
        return "Plex"
    }

    val serverName: String
        get() = resolveServerName()

    private val metadataCache = mutableMapOf<String, TautulliMetadata>()
    private val cacheMutex = Mutex()

    override suspend fun testConnection(): NetworkResult<Unit> = when (val status = getServerStatus()) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> NetworkResult.Error(code = status.code, message = status.message, cause = status.cause, errorType = status.errorType)
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun getServerStatus(): NetworkResult<TautulliServerStatus> = tautulliClient.getServerStatus().onSuccess { status ->
        _serverStatus.value = status
    }

    suspend fun refreshStreams(): NetworkResult<TautulliActivity> {
        if (_serverStatus.value == null) {
            tautulliClient.getServerStatus().onSuccess { status ->
                _serverStatus.value = status
            }
        }
        return tautulliClient
            .getActivity()
            .onSuccess { act ->
                _activity.value = act
                _activeStreams.value = act.sessions
                _streamSessions.value = act.sessions.map { it.toPlaybackStreamSession() }
                updateSummaryStats(act)
            }.onError { _, _, _ ->
                _activity.value = null
                _activeStreams.value = emptyList()
                _streamSessions.value = emptyList()
                _summaryStats.value = null
            }
    }

    override suspend fun refreshPlaybackStreams(): NetworkResult<List<PlaybackStreamSession>> = when (val result = refreshStreams()) {
        is NetworkResult.Success -> NetworkResult.Success(_streamSessions.value)
        is NetworkResult.Error -> NetworkResult.Error(code = result.code, message = result.message, cause = result.cause, errorType = result.errorType)
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun refreshHomeStats(
        timeRange: Int = 30,
        statsType: TautulliStatsType = TautulliStatsType.Plays,
    ): NetworkResult<List<TautulliHomeStatSection>> = tautulliClient
        .getHomeStats(timeRange = timeRange, statsType = statsType)
        .onSuccess { _homeStats.value = it }

    override suspend fun refreshPlaybackStats(): NetworkResult<PlaybackSummaryStats> {
        val actResult = refreshStreams()
        val stats = _summaryStats.value ?: PlaybackSummaryStats()
        return when (actResult) {
            is NetworkResult.Success -> NetworkResult.Success(stats)
            is NetworkResult.Error -> NetworkResult.Error(code = actResult.code, message = actResult.message)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun terminateSession(
        sessionId: String? = null,
        sessionKey: String? = null,
        message: String? = null,
    ): NetworkResult<Unit> = tautulliClient
        .terminateSession(sessionId = sessionId, sessionKey = sessionKey, message = message)
        .onSuccess {
            refreshStreams()
        }

    override suspend fun terminatePlaybackStream(
        sessionId: String,
        message: String?,
    ): NetworkResult<Unit> = terminateSession(sessionId = sessionId, message = message)

    suspend fun getHistory(
        start: Int? = 0,
        length: Int? = 25,
        search: String? = null,
        userId: Long? = null,
        ratingKey: String? = null,
        mediaType: TautulliMediaType? = null,
    ): NetworkResult<TautulliHistoryResponse> = tautulliClient.getHistory(
        start = start,
        length = length,
        search = search,
        userId = userId,
        ratingKey = ratingKey,
        mediaType = mediaType,
    )

    override suspend fun getPlaybackHistory(
        pageOrStart: Int,
        pageSize: Int,
    ): NetworkResult<List<PlaybackHistoryItem>> = when (val result = getHistory(start = pageOrStart, length = pageSize)) {
        is NetworkResult.Success -> {
            val items = result.data.data.map { it.toPlaybackHistoryItem() }
            NetworkResult.Success(items)
        }
        is NetworkResult.Error -> NetworkResult.Error(code = result.code, message = result.message)
        is NetworkResult.Loading -> NetworkResult.Loading
    }

    suspend fun getUsers(): NetworkResult<List<TautulliUser>> = tautulliClient.getUsers()

    suspend fun getUser(userId: Long): NetworkResult<TautulliUser> = tautulliClient.getUser(userId)

    suspend fun getUserWatchTimeStats(userId: Long): NetworkResult<List<TautulliUserWatchTime>> = tautulliClient.getUserWatchTimeStats(userId)

    fun buildPmsImageUrl(imagePath: String?): String? = buildPmsImageUrl(instance, imagePath)

    suspend fun getLibraries(): NetworkResult<List<TautulliLibrary>> = tautulliClient.getLibraries()

    suspend fun getMetadata(ratingKey: String): NetworkResult<TautulliMetadata> {
        cacheMutex.withLock {
            metadataCache[ratingKey]?.let { return NetworkResult.Success(it) }
        }
        val result = tautulliClient.getMetadata(ratingKey)
        if (result is NetworkResult.Success) {
            cacheMutex.withLock {
                metadataCache[ratingKey] = result.data
            }
        }
        return result
    }

    private fun updateSummaryStats(activity: TautulliActivity) {
        val totalSessions = activity.sessions
        val transcodeCount = totalSessions.count { it.isTranscoding }
        val directPlayCount = totalSessions.count { it.isDirectPlay }
        val activeUsersCount = totalSessions.mapNotNull { it.user?.takeIf { u -> u.isNotBlank() } ?: it.effectiveUser.takeIf { u -> u.isNotBlank() } }.distinct().size
        _summaryStats.value =
            PlaybackSummaryStats(
                activeStreamsCount = activity.streamCountInt,
                transcodeCount = transcodeCount,
                directPlayCount = directPlayCount,
                totalBandwidthKbps = activity.totalBandwidth ?: 0L,
                lanBandwidthKbps = activity.lanBandwidth ?: 0L,
                wanBandwidthKbps = activity.wanBandwidth ?: 0L,
                activeUsersCount = activeUsersCount,
            )
    }

    private fun TautulliSession.toPlaybackStreamSession(): PlaybackStreamSession {
        val mediaTypeEnum =
            when (mediaType) {
                TautulliMediaType.Movie -> PlaybackMediaType.Movie
                TautulliMediaType.Show, TautulliMediaType.Season, TautulliMediaType.Episode -> PlaybackMediaType.Episode
                TautulliMediaType.Artist, TautulliMediaType.Album, TautulliMediaType.Track -> PlaybackMediaType.Track
                else -> PlaybackMediaType.Other
            }
        val decision =
            when (transcodeDecision) {
                TautulliStreamDecision.DirectPlay -> PlaybackStreamDecision.DirectPlay
                TautulliStreamDecision.Copy, TautulliStreamDecision.DirectStream -> PlaybackStreamDecision.DirectStream
                TautulliStreamDecision.Transcode -> PlaybackStreamDecision.Transcode
                else -> PlaybackStreamDecision.Unknown
            }
        val posterPath = grandparentThumb?.takeIf { it.isNotBlank() }
            ?: parentThumb?.takeIf { it.isNotBlank() }
            ?: thumb?.takeIf { it.isNotBlank() }
        val backdropPath = art?.takeIf { it.isNotBlank() } ?: thumb?.takeIf { it.isNotBlank() }
        val totalMs = duration?.toLongOrNull()
        val progMs = viewOffset?.toLongOrNull() ?: totalMs?.let { tot ->
            (tot * progressFloat).toLong()
        }

        val subtitleText =
            when (mediaType) {
                TautulliMediaType.Episode -> {
                    val sNum = parentMediaIndex?.toIntOrNull()
                        ?: Regex("""\d+""").find(parentTitle ?: "")?.value?.toIntOrNull()
                    val eNum = mediaIndex?.toIntOrNull()
                        ?: Regex("""[eE](\d+)""").find(fullTitle ?: "")?.groupValues?.getOrNull(1)?.toIntOrNull()

                    val s = sNum?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                    val e = eNum?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                    val epTitle = title ?: ""
                    if (sNum != null || eNum != null) {
                        if (epTitle.isNotBlank()) "S$s E$e · $epTitle" else "S$s E$e"
                    } else {
                        listOfNotNull(parentTitle, epTitle).filter { it.isNotBlank() }.joinToString(" · ")
                    }
                }
                TautulliMediaType.Movie -> {
                    listOfNotNull(year, title ?: fullTitle).filter { it.isNotBlank() }.joinToString(" · ")
                }
                TautulliMediaType.Track -> {
                    listOfNotNull(grandparentTitle, parentTitle, title).filter { it.isNotBlank() }.joinToString(" · ")
                }
                else -> listOfNotNull(parentTitle, title ?: fullTitle).filter { it.isNotBlank() }.joinToString(" · ")
            }

        val vDecision = when (videoDecision) {
            TautulliStreamDecision.DirectPlay -> PlaybackStreamDecision.DirectPlay
            TautulliStreamDecision.Copy, TautulliStreamDecision.DirectStream -> PlaybackStreamDecision.DirectStream
            TautulliStreamDecision.Transcode -> PlaybackStreamDecision.Transcode
            else -> decision
        }
        val aDecision = when (audioDecision) {
            TautulliStreamDecision.DirectPlay -> PlaybackStreamDecision.DirectPlay
            TautulliStreamDecision.Copy, TautulliStreamDecision.DirectStream -> PlaybackStreamDecision.DirectStream
            TautulliStreamDecision.Transcode -> PlaybackStreamDecision.Transcode
            else -> decision
        }
        val res = (streamVideoFullResolution ?: videoFullResolution ?: streamVideoResolution ?: videoResolution)?.let {
            if (!it.endsWith("p") && it.all { c -> c.isDigit() }) "${it}p" else it
        }

        return PlaybackStreamSession(
            sessionId = id,
            sessionKey = sessionKey,
            instanceId = instance.id,
            instanceLabel = instance.label,
            instanceType = instance.type,
            serverName = resolveServerName(serverName, pmsName),
            serverType = TracearrServerType.Plex,
            ratingKey = ratingKey,
            mediaType = mediaTypeEnum,
            title = title ?: fullTitle ?: "",
            parentTitle = parentTitle,
            grandparentTitle = grandparentTitle,
            displayTitle = effectiveTitle,
            subtitle = subtitleText,
            year = year,
            username = user ?: "Unknown",
            userDisplayName = effectiveUser,
            userAvatarUrl = buildPmsImageUrl(userThumb),
            player = player,
            device = device,
            platform = platform ?: platformName,
            ipAddress = ipAddress ?: ipAddressPublic,
            location = location?.value,
            progressPercent = progressFloat,
            progressMs = progMs,
            totalDurationMs = totalMs,
            state = state?.value,
            streamDecision = decision,
            isTranscoding = isTranscoding,
            videoCodec = streamVideoCodec ?: videoCodec,
            videoResolution = res,
            transcodeVideoCodec = transcodeVideoCodec ?: streamVideoCodec.takeIf { isTranscoding },
            audioCodec = streamAudioCodec ?: audioCodec,
            transcodeAudioCodec = transcodeAudioCodec ?: streamAudioCodec.takeIf { isTranscoding },
            bandwidthKbps = bandwidth?.toLongOrNull(),
            posterUrl = buildPmsImageUrl(posterPath),
            backdropUrl = buildPmsImageUrl(backdropPath),
            product = product,
            qualityProfile = qualityProfile,
            container = container ?: streamContainer,
            videoDecision = vDecision,
            audioDecision = aDecision,
            canTerminate = true,
        )
    }

    private fun TautulliHistoryItem.toPlaybackHistoryItem(): PlaybackHistoryItem {
        val mediaTypeEnum =
            when (mediaType) {
                TautulliMediaType.Movie -> PlaybackMediaType.Movie
                TautulliMediaType.Show, TautulliMediaType.Season, TautulliMediaType.Episode -> PlaybackMediaType.Episode
                TautulliMediaType.Artist, TautulliMediaType.Album, TautulliMediaType.Track -> PlaybackMediaType.Track
                else -> PlaybackMediaType.Other
            }
        val decision =
            when (transcodeDecision) {
                TautulliStreamDecision.DirectPlay -> PlaybackStreamDecision.DirectPlay
                TautulliStreamDecision.Copy, TautulliStreamDecision.DirectStream -> PlaybackStreamDecision.DirectStream
                TautulliStreamDecision.Transcode -> PlaybackStreamDecision.Transcode
                else -> PlaybackStreamDecision.Unknown
            }
        val posterPath = grandparentThumb?.takeIf { it.isNotBlank() }
            ?: parentThumb?.takeIf { it.isNotBlank() }
            ?: thumb?.takeIf { it.isNotBlank() }

        val displayTitleText =
            when (mediaType) {
                TautulliMediaType.Episode -> {
                    grandparentTitle?.takeIf { it.isNotBlank() } ?: parentTitle?.takeIf { it.isNotBlank() } ?: title ?: fullTitle ?: ""
                }
                TautulliMediaType.Track -> {
                    grandparentTitle?.takeIf { it.isNotBlank() } ?: title ?: fullTitle ?: ""
                }
                else -> title ?: fullTitle ?: ""
            }

        val subtitleText =
            when (mediaType) {
                TautulliMediaType.Episode -> {
                    val sNum = parentMediaIndex?.toIntOrNull()
                        ?: Regex("""\d+""").find(parentTitle ?: "")?.value?.toIntOrNull()
                    val eNum = mediaIndex?.toIntOrNull()
                        ?: Regex("""[eE](\d+)""").find(fullTitle ?: "")?.groupValues?.getOrNull(1)?.toIntOrNull()

                    val s = sNum?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                    val e = eNum?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                    val epTitle = title ?: ""
                    if (sNum != null || eNum != null) {
                        if (epTitle.isNotBlank()) "S$s E$e · $epTitle" else "S$s E$e"
                    } else {
                        listOfNotNull(parentTitle, epTitle).filter { it.isNotBlank() }.joinToString(" · ")
                    }
                }
                TautulliMediaType.Movie -> {
                    listOfNotNull(year?.toString(), title ?: fullTitle).filter { it.isNotBlank() }.joinToString(" · ")
                }
                TautulliMediaType.Track -> {
                    listOfNotNull(grandparentTitle, parentTitle, title).filter { it.isNotBlank() }.joinToString(" · ")
                }
                else -> listOfNotNull(parentTitle, title ?: fullTitle).filter { it.isNotBlank() }.joinToString(" · ")
            }

        val res = videoResolution?.let { if (!it.endsWith("p") && it.all { c -> c.isDigit() }) "${it}p" else it } ?: "1080p"

        return PlaybackHistoryItem(
            id = id?.toString() ?: rowId?.toString() ?: "$ratingKey-$date",
            instanceId = instance.id,
            instanceType = instance.type,
            instanceLabel = instance.label,
            dateEpochSeconds = date ?: 0L,
            username = user ?: "Unknown",
            userDisplayName = effectiveUser,
            userAvatarUrl = buildPmsImageUrl(userThumb),
            title = title ?: fullTitle ?: "",
            displayTitle = displayTitleText,
            mediaType = mediaTypeEnum,
            ratingKey = ratingKey,
            player = player,
            platform = platform,
            ipAddress = ipAddress,
            durationSeconds = duration,
            percentComplete = percentComplete,
            streamDecision = decision,
            posterUrl = buildPmsImageUrl(posterPath),
            serverName = resolveServerName(serverName),
            serverType = TracearrServerType.Plex,
            subtitle = subtitleText,
            resolution = res,
            watched = (percentComplete ?: 0) >= 90,
        )
    }
}

fun buildPmsImageUrl(instance: Instance, imagePath: String?): String? {
    if (imagePath.isNullOrBlank()) return null
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath
    val base = instance.getEffectiveBaseUrl().trimEnd('/')
    val apiKey = instance.apiKey.value
    val encodedImg = imagePath.encodeURLQueryComponent()
    return "$base/api/v2?cmd=pms_image_proxy&img=$encodedImg&apikey=$apiKey"
}
