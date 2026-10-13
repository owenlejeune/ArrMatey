package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.repository.PlaybackMonitoringRepository
import com.dnfapps.arrmatey.instances.repository.TautulliRepository
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetPlaybackMonitoringRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.model.TracearrMediaUiState
import com.dnfapps.arrmatey.model.TracearrStatsWindowType
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHistoryItem
import com.dnfapps.arrmatey.tautulli.api.model.TautulliMediaType
import com.dnfapps.arrmatey.tautulli.api.model.TautulliSession
import com.dnfapps.arrmatey.tautulli.api.model.TautulliStreamDecision
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaServerStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaStatsCombined
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaStatsWindow
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaStatsWindows
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaWatcher
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaWatcherUser
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaWatchers
import com.dnfapps.arrmatey.tracearr.api.model.TracearrServerType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamDecision
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUser
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

data class MediaMonitoringTarget(
    val ref: String? = null,
    val title: String? = null,
    val year: Int? = null,
    val isMovie: Boolean = false,
    val isTv: Boolean = false,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val tmdbId: Long? = null,
    val tvdbId: Long? = null,
)

private data class RepoMonitoringData(
    val activeStreams: List<TracearrStreamSession>,
    val historyItems: List<TracearrHistoryItem>,
    val stats: TracearrMediaStats?,
    val watchers: List<TracearrMediaWatcher>,
    val hasMoreHistory: Boolean,
)

class UnifiedMediaDetailsTracearrHandler {
    private val _tracearrState = MutableStateFlow(TracearrMediaUiState())
    val tracearrState: StateFlow<TracearrMediaUiState> = _tracearrState.asStateFlow()

    private var currentMonitoringTarget: MediaMonitoringTarget? = null
    private var currentMonitoringRepos: List<PlaybackMonitoringRepository> = emptyList()
    private val tautulliRatingKeys = mutableMapOf<Long, String?>()
    private val repoNextCursors = mutableMapOf<Long, String?>()

    fun observeTracearrData(
        scope: CoroutineScope,
        uiStateFlow: Flow<UnifiedMediaDetailsUiState>,
        getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
        getPlaybackMonitoringRepositoryUseCase: GetPlaybackMonitoringRepositoryUseCase? = null,
        preferencesStore: PreferencesStore,
        initialTmdbId: Long?,
        initialRequestType: RequestType?,
    ) {
        val targetFlow =
            uiStateFlow
                .map { state ->
                    val success = state as? UnifiedMediaDetailsUiState.Success
                    val targetItem = success?.arrMedia
                    val seerrMedia = success?.seerrMedia

                    val resolvedTmdbId =
                        initialTmdbId ?: when (targetItem) {
                            is ArrMovie -> targetItem.tmdbId.takeIf { it > 0 }
                            is ArrSeries -> targetItem.tmdbId?.takeIf { it > 0 }
                            else -> null
                        } ?: (seerrMedia as? MovieDetails)?.id ?: (seerrMedia as? TvDetails)?.id

                    val resolvedTvdbId =
                        when (targetItem) {
                            is ArrSeries -> targetItem.tvdbId.takeIf { it > 0 }
                            else -> null
                        } ?: (seerrMedia as? TvDetails)?.externalIds?.tvdbId?.takeIf { it > 0 }

                    val resolvedReqType =
                        initialRequestType ?: when (targetItem) {
                            is ArrMovie -> RequestType.Movie
                            is ArrSeries -> RequestType.Tv
                            else -> null
                        } ?: if (seerrMedia is MovieDetails) {
                            RequestType.Movie
                        } else if (seerrMedia is TvDetails) {
                            RequestType.Tv
                        } else {
                            null
                        }

                    val isMovie = resolvedReqType == RequestType.Movie
                    val isTv = resolvedReqType == RequestType.Tv
                    val isMovieOrTv = isMovie || isTv

                    val ref =
                        if (resolvedTmdbId != null && resolvedTmdbId > 0 && isMovieOrTv) {
                            if (isTv) "show:tmdb:$resolvedTmdbId" else "movie:tmdb:$resolvedTmdbId"
                        } else {
                            null
                        }

                    val title =
                        targetItem?.title
                            ?: seerrMedia?.displayTitle
                            ?: (seerrMedia as? MovieDetails)?.title
                            ?: (seerrMedia as? TvDetails)?.name
                            ?: success?.displayTitle

                    val year =
                        (targetItem as? ArrMovie)?.year
                            ?: (targetItem as? ArrSeries)?.year
                            ?: success?.year?.toIntOrNull()

                    MediaMonitoringTarget(
                        ref = ref,
                        title = title,
                        year = year,
                        isMovie = isMovie,
                        isTv = isTv,
                        tmdbId = resolvedTmdbId,
                        tvdbId = resolvedTvdbId,
                    )
                }.distinctUntilChanged()

        observeMonitoringData(
            scope = scope,
            targetFlow = targetFlow,
            getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
            getPlaybackMonitoringRepositoryUseCase = getPlaybackMonitoringRepositoryUseCase,
            preferencesStore = preferencesStore,
        )
    }

    fun observeTracearrData(
        scope: CoroutineScope,
        uiStateFlow: Flow<UnifiedMediaDetailsUiState>,
        getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
        preferencesStore: PreferencesStore,
        initialTmdbId: Long?,
        initialRequestType: RequestType?,
    ) = observeTracearrData(
        scope = scope,
        uiStateFlow = uiStateFlow,
        getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
        getPlaybackMonitoringRepositoryUseCase = null,
        preferencesStore = preferencesStore,
        initialTmdbId = initialTmdbId,
        initialRequestType = initialRequestType,
    )

    fun observeTracearrDataForRef(
        scope: CoroutineScope,
        refFlow: Flow<String?>,
        getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
        preferencesStore: PreferencesStore,
        getPlaybackMonitoringRepositoryUseCase: GetPlaybackMonitoringRepositoryUseCase? = null,
    ) {
        val targetFlow =
            refFlow
                .map { ref -> MediaMonitoringTarget(ref = ref) }
                .distinctUntilChanged()

        observeMonitoringData(
            scope = scope,
            targetFlow = targetFlow,
            getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
            getPlaybackMonitoringRepositoryUseCase = getPlaybackMonitoringRepositoryUseCase,
            preferencesStore = preferencesStore,
        )
    }

    fun observeTracearrDataForEpisode(
        scope: CoroutineScope,
        episodeFlow: Flow<Episode>,
        seriesTitleFlow: Flow<String?>,
        getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
        getPlaybackMonitoringRepositoryUseCase: GetPlaybackMonitoringRepositoryUseCase? = null,
        preferencesStore: PreferencesStore,
    ) {
        val targetFlow =
            combine(episodeFlow, seriesTitleFlow) { episode, seriesTitle ->
                val tvdbId = episode.tvdbId?.takeIf { it > 0 }
                val ref = tvdbId?.let { "episode:tvdb:$it" }
                val title = seriesTitle ?: episode.series?.title
                MediaMonitoringTarget(
                    ref = ref,
                    title = title,
                    seasonNumber = episode.seasonNumber,
                    episodeNumber = episode.episodeNumber,
                    episodeTitle = episode.title,
                    isTv = true,
                    isMovie = false,
                    tvdbId = tvdbId,
                )
            }.distinctUntilChanged()

        observeMonitoringData(
            scope = scope,
            targetFlow = targetFlow,
            getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
            getPlaybackMonitoringRepositoryUseCase = getPlaybackMonitoringRepositoryUseCase,
            preferencesStore = preferencesStore,
        )
    }

    fun observeMonitoringData(
        scope: CoroutineScope,
        targetFlow: Flow<MediaMonitoringTarget>,
        getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
        getPlaybackMonitoringRepositoryUseCase: GetPlaybackMonitoringRepositoryUseCase? = null,
        preferencesStore: PreferencesStore,
    ) {
        val monitoringReposFlow: Flow<List<PlaybackMonitoringRepository>> =
            getPlaybackMonitoringRepositoryUseCase?.observeAll()
                ?: getTracearrInstanceRepositoryUseCase.observeSelected().map { listOfNotNull(it) }

        scope.launch {
            combine(
                targetFlow,
                monitoringReposFlow,
                preferencesStore.tracearrDetailsIntegration,
            ) { target, repos, enabled ->
                Triple(target, repos, enabled)
            }.collectLatest { (target, repos, enabled) ->
                if (repos.isEmpty() || !enabled) {
                    _tracearrState.value = TracearrMediaUiState(isTracearrConfigured = false)
                    currentMonitoringTarget = null
                    currentMonitoringRepos = emptyList()
                    tautulliRatingKeys.clear()
                    repoNextCursors.clear()
                    return@collectLatest
                }

                currentMonitoringTarget = target
                currentMonitoringRepos = repos
                tautulliRatingKeys.clear()
                repoNextCursors.clear()
                _tracearrState.update { it.copy(isTracearrConfigured = true, isLoading = true) }

                coroutineScope {
                    val deferredResults =
                        repos.map { repo ->
                            async {
                                when (repo) {
                                    is TracearrRepository -> fetchTracearrData(repo, target)
                                    is TautulliRepository -> fetchTautulliData(repo, target)
                                    else -> null
                                }
                            }
                        }
                    val results = deferredResults.awaitAll().filterNotNull()

                    val allActive = results.flatMap { it.activeStreams }
                    val allHistory =
                        results.flatMap { it.historyItems }.sortedByDescending {
                            it.startedAt ?: it.stoppedAt ?: Instant.fromEpochMilliseconds(0)
                        }
                    val combinedStats = combineStats(results.mapNotNull { it.stats })
                    val combinedWatchers = combineWatchers(results.map { it.watchers })
                    val hasMore = results.any { it.hasMoreHistory }

                    _tracearrState.update { currentState ->
                        currentState.copy(
                            isTracearrConfigured = true,
                            stats = combinedStats,
                            watchers = TracearrMediaWatchers(watchers = combinedWatchers),
                            activeStreams = allActive,
                            historyItems = allHistory,
                            nextHistoryCursor = if (hasMore) "combined_more" else null,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchTracearrData(
        repo: TracearrRepository,
        target: MediaMonitoringTarget,
    ): RepoMonitoringData? {
        val ref = target.ref
        if (ref.isNullOrBlank()) {
            val activeSessions = repo.activeStreams.value.filter { sessionMatchesTarget(it, target) }
            return RepoMonitoringData(
                activeStreams = activeSessions,
                historyItems = emptyList(),
                stats = null,
                watchers = emptyList(),
                hasMoreHistory = false,
            )
        }

        repo.getPublicStreams()
        val statsResult = repo.getMediaStats(ref)
        val watchersResult = repo.getMediaWatchers(ref)
        val historyResult = repo.getMediaHistory(ref, cursor = null, pageSize = 25)

        val stats = (statsResult as? NetworkResult.Success)?.data
        val watchers = (watchersResult as? NetworkResult.Success)?.data?.watchers ?: emptyList()
        val history = (historyResult as? NetworkResult.Success)?.data?.data ?: emptyList()
        val nextCursor = (historyResult as? NetworkResult.Success)?.data?.meta?.nextCursor
        repoNextCursors[repo.instance.id] = nextCursor

        val activeSessions = repo.activeStreams.value.filter { sessionMatchesTarget(it, target) }
        return RepoMonitoringData(
            activeStreams = activeSessions,
            historyItems = history,
            stats = stats,
            watchers = watchers,
            hasMoreHistory = nextCursor != null,
        )
    }

    private suspend fun fetchTautulliData(
        repo: TautulliRepository,
        target: MediaMonitoringTarget,
    ): RepoMonitoringData? {
        val title = target.title ?: return null

        val tautulliMediaType = if (target.isMovie) TautulliMediaType.Movie else TautulliMediaType.Episode
        val primarySearch = title
        var rawItems =
            when (val res = repo.getHistory(start = 0, length = 100, search = primarySearch, mediaType = tautulliMediaType)) {
                is NetworkResult.Success -> res.data.data
                else -> emptyList()
            }
        var filtered = rawItems.filter { matchesTarget(it, target) }

        if (filtered.isEmpty() && primarySearch.contains(Regex("""[^a-zA-Z0-9 ]"""))) {
            val cleanSearch = primarySearch.replace(Regex("""[^a-zA-Z0-9 ]"""), " ").trim()
            val retryItems =
                when (val res = repo.getHistory(start = 0, length = 100, search = cleanSearch, mediaType = tautulliMediaType)) {
                    is NetworkResult.Success -> res.data.data
                    else -> emptyList()
                }
            val retryFiltered = retryItems.filter { matchesTarget(it, target) }
            if (retryFiltered.isNotEmpty()) {
                rawItems = retryItems
                filtered = retryFiltered
            }
        }

        if (filtered.isEmpty()) {
            val fallbackItems =
                when (val res = repo.getHistory(start = 0, length = 100, search = primarySearch)) {
                    is NetworkResult.Success -> res.data.data
                    else -> emptyList()
                }
            val fallbackFiltered = fallbackItems.filter { matchesTarget(it, target) }
            if (fallbackFiltered.isNotEmpty()) {
                rawItems = fallbackItems
                filtered = fallbackFiltered
            }
        }

        val resolvedRatingKey =
            filtered.firstNotNullOfOrNull {
                if (target.seasonNumber != null && target.episodeNumber != null) {
                    it.ratingKey
                } else if (target.isMovie) {
                    it.ratingKey
                } else {
                    it.grandparentRatingKey ?: it.ratingKey
                }
            }
        tautulliRatingKeys[repo.instance.id] = resolvedRatingKey

        repo.refreshStreams()
        val activeSessions = repo.activeStreams.value.filter { sessionMatchesTarget(it, target, resolvedRatingKey) }
        val effectiveServerName = repo.resolveServerName(repo.instance.label)
        val activeStreamSessions = activeSessions.map { it.toTracearrStreamSession(effectiveServerName) }
        val activeWatchers = activeSessions.map { it.toTracearrMediaWatcher() }

        val stats = buildStatsFromTautulli(filtered, repo)
        val topWatchers = buildTopWatchersFromTautulli(filtered, target.isTv)
        val historyItems = filtered.map { it.toTracearrHistoryItem(repo, target.tmdbId, target.tvdbId) }

        val hasMore = rawItems.size >= 100
        repoNextCursors[repo.instance.id] = if (hasMore) "100" else null

        return RepoMonitoringData(
            activeStreams = activeStreamSessions,
            historyItems = historyItems,
            stats = stats,
            watchers = activeWatchers + topWatchers,
            hasMoreHistory = hasMore,
        )
    }

    private fun matchesTarget(item: TautulliHistoryItem, target: MediaMonitoringTarget): Boolean {
        if (target.seasonNumber != null && target.episodeNumber != null) {
            val sNum = item.parentMediaIndex?.toIntOrNull()
            val eNum = item.mediaIndex?.toIntOrNull()
            val titleMatch =
                target.title == null ||
                    item.grandparentTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                    item.parentTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                    item.fullTitle?.contains(target.title.trim(), ignoreCase = true) == true
            return titleMatch && sNum == target.seasonNumber && eNum == target.episodeNumber
        }
        if (target.isMovie) {
            val titleMatches =
                target.title != null &&
                    (
                        item.title?.trim().equals(target.title.trim(), ignoreCase = true) ||
                            item.fullTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                            item.fullTitle?.contains(target.title.trim(), ignoreCase = true) == true
                        )
            val yearMatches = target.year == null || item.year == null || item.year == target.year
            return item.mediaType == TautulliMediaType.Movie && titleMatches && yearMatches
        }
        if (target.isTv) {
            return target.title != null &&
                (
                    item.grandparentTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                        item.title?.trim().equals(target.title.trim(), ignoreCase = true) ||
                        item.fullTitle?.contains(target.title.trim(), ignoreCase = true) == true
                    )
        }
        return target.title != null &&
            (
                item.title?.trim().equals(target.title.trim(), ignoreCase = true) ||
                    item.fullTitle?.trim().equals(target.title.trim(), ignoreCase = true)
                )
    }

    private fun sessionMatchesTarget(
        session: TautulliSession,
        target: MediaMonitoringTarget,
        ratingKey: String?,
    ): Boolean {
        if (ratingKey != null && (session.ratingKey == ratingKey || session.grandparentRatingKey == ratingKey)) {
            return true
        }
        if (target.seasonNumber != null && target.episodeNumber != null) {
            val sNum = session.parentMediaIndex?.toIntOrNull()
            val eNum = session.mediaIndex?.toIntOrNull()
            val titleMatch =
                target.title == null ||
                    session.grandparentTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                    session.parentTitle?.trim().equals(target.title.trim(), ignoreCase = true)
            return titleMatch && sNum == target.seasonNumber && eNum == target.episodeNumber
        }
        if (target.isMovie) {
            val titleMatches =
                target.title != null &&
                    (
                        session.title?.trim().equals(target.title.trim(), ignoreCase = true) ||
                            session.fullTitle?.trim().equals(target.title.trim(), ignoreCase = true)
                        )
            return session.mediaType == TautulliMediaType.Movie && titleMatches
        }
        if (target.isTv) {
            return target.title != null &&
                (
                    session.grandparentTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                        session.title?.trim().equals(target.title.trim(), ignoreCase = true)
                    )
        }
        return target.title != null &&
            (
                session.title?.trim().equals(target.title.trim(), ignoreCase = true) ||
                    session.fullTitle?.trim().equals(target.title.trim(), ignoreCase = true)
                )
    }

    private fun sessionMatchesTarget(
        session: TracearrStreamSession,
        target: MediaMonitoringTarget,
    ): Boolean {
        if (target.seasonNumber != null && target.episodeNumber != null) {
            val sNum = session.seasonNumber
            val eNum = session.episodeNumber
            val titleMatch =
                target.title == null ||
                    session.grandparentTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                    session.showTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                    session.mediaTitle?.contains(target.title.trim(), ignoreCase = true) == true
            val idMatch = target.tvdbId != null && session.tvdbId == target.tvdbId
            return (titleMatch || idMatch) && sNum == target.seasonNumber && eNum == target.episodeNumber
        }
        if (target.isMovie) {
            val idMatch = target.tmdbId != null && session.tmdbId == target.tmdbId
            val titleMatch =
                target.title != null &&
                    (
                        session.mediaTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                            session.mediaTitle?.contains(target.title.trim(), ignoreCase = true) == true
                        )
            val yearMatches = target.year == null || session.year == null || session.year == target.year
            return idMatch || (session.mediaType == TracearrMediaType.Movie && titleMatch && yearMatches)
        }
        if (target.isTv) {
            val idMatch = target.tvdbId != null && session.tvdbId == target.tvdbId
            val titleMatch =
                target.title != null &&
                    (
                        session.grandparentTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                            session.showTitle?.trim().equals(target.title.trim(), ignoreCase = true) ||
                            session.mediaTitle?.trim().equals(target.title.trim(), ignoreCase = true)
                        )
            return idMatch || titleMatch
        }
        return target.title != null && session.mediaTitle?.trim().equals(target.title.trim(), ignoreCase = true)
    }

    private fun TautulliSession.toTracearrStreamSession(effectiveServer: String?): TracearrStreamSession {
        val totalMs = duration?.toLongOrNull()
        val progMs = viewOffset?.toLongOrNull() ?: totalMs?.let { tot -> (tot * progressFloat).toLong() }
        val sNum = parentMediaIndex?.toIntOrNull()
        val eNum = mediaIndex?.toIntOrNull()
        val isMovie = mediaType == TautulliMediaType.Movie
        val isEpisode = mediaType == TautulliMediaType.Episode
        val uName = user ?: friendlyName ?: "Unknown"
        val fName = friendlyName ?: user
        val mType =
            when (mediaType) {
                TautulliMediaType.Movie -> TracearrMediaType.Movie
                TautulliMediaType.Episode -> TracearrMediaType.Episode
                TautulliMediaType.Track -> TracearrMediaType.Track
                else -> TracearrMediaType.Unknown
            }
        val vDec =
            when (transcodeDecision) {
                TautulliStreamDecision.DirectPlay -> TracearrStreamDecision.DirectPlay
                TautulliStreamDecision.Copy, TautulliStreamDecision.DirectStream -> TracearrStreamDecision.Copy
                TautulliStreamDecision.Transcode -> TracearrStreamDecision.Transcode
                else -> null
            }
        return TracearrStreamSession(
            id = sessionKey ?: id,
            sessionKey = sessionKey,
            serverId = null,
            serverName = effectiveServer ?: serverName ?: "Plex",
            serverType = TracearrServerType.Plex,
            serverUserId = userId?.toString(),
            state = state?.value ?: "playing",
            mediaType = mType,
            mediaTitle = title ?: fullTitle,
            grandparentTitle = grandparentTitle,
            showTitle = grandparentTitle,
            seasonNumber = sNum,
            episodeNumber = eNum,
            year = year?.toIntOrNull(),
            posterUrl = null,
            ratingKey = ratingKey,
            startedAt = null,
            durationMs = totalMs,
            totalDurationMs = totalMs,
            progressMs = progMs,
            user =
            TracearrUser(
                id = userId?.toString() ?: "",
                username = uName,
                identityName = fName,
                avatarUrl = userThumb,
            ),
            username = uName,
            userId = userId?.toString(),
            userThumb = userThumb,
            product = product,
            player = player,
            device = device,
            platform = platform,
            isTranscode = transcodeDecision == TautulliStreamDecision.Transcode,
            videoDecision = vDec,
            streamVideoCodec = transcodeVideoCodec ?: videoCodec,
            streamAudioCodec = transcodeAudioCodec ?: audioCodec,
            resolution = videoResolution,
            bitrate = bandwidth?.toIntOrNull(),
            canTerminate = true,
        )
    }

    private fun TautulliSession.toTracearrMediaWatcher(): TracearrMediaWatcher {
        val userDisplayName = friendlyName ?: user ?: "Unknown"
        val userName = user ?: friendlyName ?: "Unknown"
        val dur = duration?.toLongOrNull() ?: 0L
        val prog = viewOffset?.toLongOrNull() ?: (dur * progressFloat).toLong()
        return TracearrMediaWatcher(
            user =
            TracearrMediaWatcherUser(
                serverUserId = userId?.toString(),
                userId = userId?.toString(),
                username = userName,
                identityName = userDisplayName,
            ),
            plays = 1,
            watchTimeMs = prog,
            completionPct = (progressFloat * 100).toInt(),
            lastWatchedDay = null,
        )
    }

    private fun TautulliHistoryItem.toTracearrHistoryItem(
        tautulliRepo: TautulliRepository,
        fallbackTmdbId: Long? = null,
        fallbackTvdbId: Long? = null,
    ): TracearrHistoryItem {
        val durationMsVal = (duration ?: 0L) * 1000L
        val progMsVal = percentComplete?.let { (durationMsVal * (it / 100.0)).toLong() }
        val serverNameResolved = tautulliRepo.resolveServerName(serverName)
        val mediaTypeEnum =
            when (mediaType) {
                TautulliMediaType.Movie -> TracearrMediaType.Movie
                TautulliMediaType.Episode -> TracearrMediaType.Episode
                TautulliMediaType.Track -> TracearrMediaType.Track
                else -> null
            }
        val thumbCandidate =
            grandparentThumb?.takeIf { it.isNotBlank() }
                ?: parentThumb?.takeIf { it.isNotBlank() }
                ?: thumb?.takeIf { it.isNotBlank() }
        val poster = tautulliRepo.buildPmsImageUrl(thumbCandidate)
        val userAvatar = tautulliRepo.buildPmsImageUrl(userThumb)
        val userObj =
            TracearrUser(
                id = userId?.toString() ?: user ?: "",
                userId = userId?.toString(),
                username = user ?: friendlyName ?: "Unknown",
                identityName = friendlyName ?: user,
                thumbUrl = userAvatar,
                avatarUrl = userAvatar,
            )
        val startedInstant = date?.let { Instant.fromEpochSeconds(it) }
        val stoppedInstant = stopped?.let { Instant.fromEpochSeconds(it) } ?: startedInstant

        return TracearrHistoryItem(
            id = id?.toString() ?: rowId?.toString() ?: "$ratingKey-$date",
            serverId = null,
            serverName = serverNameResolved,
            serverType = TracearrServerType.Plex,
            state = "stopped",
            mediaType = mediaTypeEnum,
            mediaTitle = title ?: fullTitle,
            showTitle = grandparentTitle ?: parentTitle,
            seasonNumber = parentMediaIndex?.toIntOrNull(),
            episodeNumber = mediaIndex?.toIntOrNull(),
            year = year,
            artistName = grandparentTitle,
            albumName = parentTitle,
            thumbPath = poster,
            posterUrl = poster,
            durationMs = durationMsVal,
            progressMs = progMsVal,
            totalDurationMs = durationMsVal,
            percentComplete = percentComplete?.toDouble(),
            startedAt = startedInstant,
            stoppedAt = stoppedInstant,
            watched = (percentComplete ?: 0) >= 90,
            device = platform ?: player,
            player = player,
            product = platform,
            platform = platform,
            isTranscode = transcodeDecision == TautulliStreamDecision.Transcode,
            resolution = videoResolution,
            ratingKey = ratingKey,
            parentRatingKey = parentRatingKey,
            grandparentRatingKey = grandparentRatingKey,
            grandparentTitle = grandparentTitle,
            tmdbId = fallbackTmdbId,
            tvdbId = fallbackTvdbId,
            user = userObj,
            username = user ?: friendlyName,
            userId = userId?.toString(),
            userThumb = userAvatar,
            userAvatarUrl = userAvatar,
        )
    }

    private fun buildStatsFromTautulli(
        items: List<TautulliHistoryItem>,
        tautulliRepo: TautulliRepository,
    ): TracearrMediaStats {
        val nowSeconds = Clock.System.now().epochSeconds
        val thirtyDaysAgo = nowSeconds - 30 * 86400L
        val sevenDaysAgo = nowSeconds - 7 * 86400L

        fun buildWindow(windowItems: List<TautulliHistoryItem>): TracearrMediaStatsWindow {
            val plays = windowItems.size.toLong()
            val watchTimeMs =
                windowItems.sumOf { item ->
                    val dur = (item.duration ?: 0L) * 1000L
                    val pct = (item.percentComplete?.coerceIn(0, 100) ?: 100).toDouble() / 100.0
                    (dur * pct).toLong()
                }
            val uniqueUsers = windowItems.mapNotNull { it.effectiveUser.takeIf { u -> u.isNotBlank() } }.distinct().size
            val perServer =
                windowItems.groupBy { tautulliRepo.resolveServerName(it.serverName) }.map { (sName, sItems) ->
                    TracearrMediaServerStats(
                        serverId = sName,
                        serverName = sName,
                        plays = sItems.size.toLong(),
                        watchTimeMs =
                        sItems.sumOf { item ->
                            val dur = (item.duration ?: 0L) * 1000L
                            val pct = (item.percentComplete?.coerceIn(0, 100) ?: 100).toDouble() / 100.0
                            (dur * pct).toLong()
                        },
                        uniqueUsers = sItems.mapNotNull { it.effectiveUser.takeIf { u -> u.isNotBlank() } }.distinct().size,
                    )
                }
            return TracearrMediaStatsWindow(
                combined =
                TracearrMediaStatsCombined(
                    plays = plays,
                    watchTimeMs = watchTimeMs,
                    uniqueUsers = uniqueUsers,
                ),
                perServer = perServer,
            )
        }

        return TracearrMediaStats(
            windows =
            TracearrMediaStatsWindows(
                allTime = buildWindow(items),
                last30 = buildWindow(items.filter { (it.date ?: 0L) >= thirtyDaysAgo }),
                last7 = buildWindow(items.filter { (it.date ?: 0L) >= sevenDaysAgo }),
            ),
        )
    }

    private fun buildTopWatchersFromTautulli(
        items: List<TautulliHistoryItem>,
        isTv: Boolean,
    ): List<TracearrMediaWatcher> = items.groupBy { it.effectiveUser }.map { (uName, uItems) ->
        val plays = uItems.size.toLong()
        val watchTimeMs =
            uItems.sumOf { item ->
                val dur = (item.duration ?: 0L) * 1000L
                val pct = (item.percentComplete?.coerceIn(0, 100) ?: 100).toDouble() / 100.0
                (dur * pct).toLong()
            }
        val maxPct = uItems.mapNotNull { it.percentComplete }.maxOrNull()
        val latestEpoch = uItems.mapNotNull { it.date }.maxOrNull()
        val lastDay = latestEpoch?.let { Instant.fromEpochSeconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() }
        TracearrMediaWatcher(
            user =
            TracearrMediaWatcherUser(
                serverUserId = uItems.firstOrNull()?.userId?.toString(),
                userId = uItems.firstOrNull()?.userId?.toString(),
                username = uName,
                identityName = uItems.firstOrNull()?.friendlyName ?: uName,
            ),
            plays = plays,
            watchTimeMs = watchTimeMs,
            completionPct = maxPct,
            lastWatchedDay = lastDay,
            distinctEpisodesWatched = if (isTv) uItems.mapNotNull { it.ratingKey }.distinct().size else null,
        )
    }.sortedByDescending { it.plays }

    private fun combineStats(statsList: List<TracearrMediaStats>): TracearrMediaStats? {
        if (statsList.isEmpty()) return null
        if (statsList.size == 1) return statsList.first()

        fun combineWindow(selector: (TracearrMediaStats) -> TracearrMediaStatsWindow?): TracearrMediaStatsWindow {
            val windows = statsList.mapNotNull(selector)
            val plays = windows.sumOf { it.combined?.plays ?: 0L }
            val watchTimeMs = windows.sumOf { it.combined?.watchTimeMs ?: 0L }
            val uniqueUsers = windows.sumOf { it.combined?.uniqueUsers ?: 0 }
            val perServer = windows.flatMap { it.perServer }
            return TracearrMediaStatsWindow(
                combined =
                TracearrMediaStatsCombined(
                    plays = plays,
                    watchTimeMs = watchTimeMs,
                    uniqueUsers = uniqueUsers,
                ),
                perServer = perServer,
            )
        }

        return TracearrMediaStats(
            windows =
            TracearrMediaStatsWindows(
                allTime = combineWindow { it.windows?.allTime },
                last30 = combineWindow { it.windows?.last30 },
                last7 = combineWindow { it.windows?.last7 },
            ),
        )
    }

    private fun combineWatchers(watchersList: List<List<TracearrMediaWatcher>>): List<TracearrMediaWatcher> {
        val flattened = watchersList.flatten()
        if (flattened.isEmpty()) return emptyList()

        return flattened.groupBy { it.user?.identityName ?: it.user?.username ?: "" }
            .filterKeys { it.isNotBlank() }
            .map { (_, userWatchers) ->
                val plays = userWatchers.sumOf { it.plays }
                val watchTimeMs = userWatchers.sumOf { it.watchTimeMs }
                val completionPct = userWatchers.mapNotNull { it.completionPct }.maxOrNull()
                val lastWatchedDay = userWatchers.mapNotNull { it.lastWatchedDay }.maxOrNull()
                val distinctEpisodes = userWatchers.mapNotNull { it.distinctEpisodesWatched }.takeIf { it.isNotEmpty() }?.sum()
                TracearrMediaWatcher(
                    user = userWatchers.firstOrNull()?.user,
                    plays = plays,
                    watchTimeMs = watchTimeMs,
                    completionPct = completionPct,
                    lastWatchedDay = lastWatchedDay,
                    distinctEpisodesWatched = distinctEpisodes,
                )
            }.sortedByDescending { it.plays }
    }

    fun selectTracearrStatsWindow(window: TracearrStatsWindowType) {
        _tracearrState.update { it.copy(selectedStatsWindow = window) }
    }

    fun loadMoreTracearrHistory(
        scope: CoroutineScope,
        getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
        preferencesStore: PreferencesStore,
        getPlaybackMonitoringRepositoryUseCase: GetPlaybackMonitoringRepositoryUseCase? = null,
    ) {
        val currentState = _tracearrState.value
        if (currentState.isLoadingHistoryMore) return
        val target = currentMonitoringTarget ?: return
        val repos = currentMonitoringRepos.ifEmpty { return }

        scope.launch {
            val enabled = preferencesStore.tracearrDetailsIntegration.firstOrNull() ?: false
            if (!enabled) return@launch

            _tracearrState.update { it.copy(isLoadingHistoryMore = true) }

            val newHistoryItems = mutableListOf<TracearrHistoryItem>()
            var anyHasMore = false

            repos.forEach { repo ->
                when (repo) {
                    is TracearrRepository -> {
                        val ref = target.ref ?: return@forEach
                        val cursor = repoNextCursors[repo.instance.id] ?: return@forEach
                        when (val res = repo.getMediaHistory(ref, cursor = cursor, pageSize = 25)) {
                            is NetworkResult.Success -> {
                                val data = res.data.data
                                newHistoryItems.addAll(data)
                                val next = res.data.meta?.nextCursor
                                repoNextCursors[repo.instance.id] = next
                                if (next != null) anyHasMore = true
                            }
                            else -> {}
                        }
                    }
                    is TautulliRepository -> {
                        val cursor = repoNextCursors[repo.instance.id] ?: return@forEach
                        val start = cursor.toIntOrNull() ?: 0
                        val tautulliMediaType = if (target.isMovie) TautulliMediaType.Movie else TautulliMediaType.Episode
                        val rKey = tautulliRatingKeys[repo.instance.id]
                        val res =
                            repo.getHistory(
                                start = start,
                                length = 25,
                                search = if (rKey == null) target.title else null,
                                ratingKey = rKey,
                                mediaType = tautulliMediaType,
                            )
                        when (res) {
                            is NetworkResult.Success -> {
                                val raw = res.data.data
                                val matched = raw.filter { matchesTarget(it, target) }
                                val converted = matched.map { it.toTracearrHistoryItem(repo, target.tmdbId, target.tvdbId) }
                                newHistoryItems.addAll(converted)
                                val next = if (raw.size >= 25) (start + 25).toString() else null
                                repoNextCursors[repo.instance.id] = next
                                if (next != null) anyHasMore = true
                            }
                            else -> {}
                        }
                    }
                }
            }

            _tracearrState.update { state ->
                val combined =
                    (state.historyItems + newHistoryItems)
                        .distinctBy { it.id }
                        .sortedByDescending { it.startedAt ?: it.stoppedAt ?: Instant.fromEpochMilliseconds(0) }
                state.copy(
                    historyItems = combined,
                    nextHistoryCursor = if (anyHasMore) "combined_more" else null,
                    isLoadingHistoryMore = false,
                )
            }
        }
    }
}
