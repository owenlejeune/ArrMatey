package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.MonitorNewItems
import com.dnfapps.arrmatey.arr.api.model.Quality
import com.dnfapps.arrmatey.arr.api.model.QualityInfo
import com.dnfapps.arrmatey.arr.api.model.QueueDownloadState
import com.dnfapps.arrmatey.arr.api.model.QueueDownloadStatus
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import com.dnfapps.arrmatey.arr.api.model.Revision
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.Keyword
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.Network
import com.dnfapps.arrmatey.seerr.api.model.ProductionCompany
import com.dnfapps.arrmatey.seerr.api.model.RequestMedia
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.RequestUser
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import dev.icerock.moko.resources.ImageResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

sealed interface CombinedDashboardState {
    data object Initial : CombinedDashboardState

    data object Loading : CombinedDashboardState

    data class Success(
        val instances: List<ArrInstanceDashboardState>,
        val seerrInstances: List<SeerrDashboardState> = emptyList(),
        val downloadClients: List<DownloadClientDashboardState> = emptyList(),
        val activityQueue: List<QueueItem> = emptyList(),
        val recentlyAdded: List<ArrMedia> = emptyList(),
        val downloadTransfers: List<DownloadTransferInfo> = emptyList(),
        val activeDownloads: List<DownloadItem> = emptyList(),
        val calendarItems: List<DashboardCalendarItem> = emptyList(),
        val upcomingCalendarItems: List<DashboardCalendarItem> = emptyList(),
        val prowlarrStats: List<ProwlarrDashboardState> = emptyList(),
        val bazarrStats: List<BazarrDashboardState> = emptyList(),
        val tracearrStats: List<TracearrDashboardState> = emptyList(),
        val trendingMedia: List<DiscoverResult> = emptyList(),
        val popularMovies: List<DiscoverResult> = emptyList(),
        val popularTv: List<DiscoverResult> = emptyList(),
        val upcomingMovies: List<DiscoverResult> = emptyList(),
        val upcomingTv: List<DiscoverResult> = emptyList(),
        val quickPickItem: DiscoverResult? = null,
        val networkStatus: NetworkStatusState? = null,
        val isRefreshing: Boolean = false,
    ) : CombinedDashboardState {
        val pendingRequests: List<MediaRequestPackage>
            get() = seerrInstances.flatMap { it.pendingRequests }

        val openIssues: List<MediaIssuePackage>
            get() = seerrInstances.flatMap { it.openIssues }

        val activeStreams: List<TracearrStreamSession>
            get() = tracearrStats.flatMap { it.activeStreams }

        val spotlightMedia: List<DiscoverResult>
            get() {
                val all =
                    (trendingMedia + popularMovies + popularTv)
                        .distinctBy { "${it.mediaType.name}_${it.id}" }
                val withImages = all.filter { it.backdropPath != null || it.posterPath != null }
                return withImages.ifEmpty { all }
            }

        val quickPickMedia: List<DiscoverResult>
            get() =
                (trendingMedia + popularMovies + popularTv)
                    .distinctBy { "${it.mediaType.name}_${it.id}" }

        fun getDiscoverFeedItems(category: DiscoverCategory): List<DiscoverResult> =
            when (category) {
                DiscoverCategory.TRENDING -> trendingMedia
                DiscoverCategory.POPULAR_MOVIES -> popularMovies
                DiscoverCategory.POPULAR_SERIES -> popularTv
                DiscoverCategory.UPCOMING_MOVIES,
                DiscoverCategory.UPCOMING_SERIES,
                -> upcomingMovies.ifEmpty { upcomingTv }
            }

        fun resolveMediaStatus(item: DiscoverResult): com.dnfapps.arrmatey.seerr.api.model.MediaStatus {
            val directStatus =
                item.mediaInfo?.status?.let {
                    com.dnfapps.arrmatey.seerr.api.model.MediaStatus
                        .fromValue(it)
                }
            if (directStatus != null && directStatus != com.dnfapps.arrmatey.seerr.api.model.MediaStatus.Unknown) {
                return directStatus
            }

            if (item.mediaInfo?.requests?.isNotEmpty() == true) {
                if (item.mediaInfo.requests.any { it.status == 1 }) {
                    return com.dnfapps.arrmatey.seerr.api.model.MediaStatus.Pending
                }
                if (item.mediaInfo.requests.any { it.status == 2 }) {
                    return com.dnfapps.arrmatey.seerr.api.model.MediaStatus.Processing
                }
            }

            val isPending =
                seerrInstances.any { inst ->
                    inst.pendingRequests.any { pkg ->
                        pkg.request.media.tmdbId == item.id ||
                            pkg.details?.id == item.id ||
                            (item.mediaInfo?.tmdbId != null && pkg.request.media.tmdbId == item.mediaInfo.tmdbId)
                    }
                }
            if (isPending) return com.dnfapps.arrmatey.seerr.api.model.MediaStatus.Pending

            val isInLibrary =
                instances.any { inst ->
                    inst.library.any { media ->
                        when (media) {
                            is com.dnfapps.arrmatey.arr.api.model.ArrMovie ->
                                (media.tmdbId > 0 && media.tmdbId == item.id) ||
                                    (item.mediaInfo?.tmdbId != null && media.tmdbId == item.mediaInfo.tmdbId)

                            is com.dnfapps.arrmatey.arr.api.model.ArrSeries ->
                                (media.tmdbId != null && media.tmdbId > 0 && media.tmdbId == item.id) ||
                                    (item.mediaInfo?.tvdbId != null && media.tvdbId == item.mediaInfo.tvdbId) ||
                                    (item.mediaInfo?.tmdbId != null && media.tmdbId == item.mediaInfo.tmdbId)

                            else -> false
                        }
                    }
                }
            if (isInLibrary) return com.dnfapps.arrmatey.seerr.api.model.MediaStatus.Available

            return com.dnfapps.arrmatey.seerr.api.model.MediaStatus.Unknown
        }
    }

    companion object {
        val Mock: Success by lazy {
            val now = Clock.System.now()
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

            val instances =
                InstanceType.entries.mapIndexed { index, type ->
                    Instance(
                        id = index.toLong(),
                        type = type,
                        label = type.name,
                        url = "http://localhost:${type.defaultPort}",
                        apiKey = EncryptedString("mock"),
                    )
                }

            val mockUser =
                RequestUser(
                    permissions = 0,
                    id = 1,
                    email = "captain@arrmatey.app",
                    username = "CaptainArr",
                    displayName = "Captain Arr",
                    userType = 1,
                    avatar = "",
                    createdAt = now,
                    updatedAt = now,
                    requestCount = 5,
                )

            val mockRequestMedia =
                RequestMedia(
                    id = 1,
                    mediaType = RequestType.Movie,
                    tmdbId = 1011985,
                    tvdbId = null,
                    imdbId = null,
                    status = 2,
                    status4k = 1,
                    createdAt = now,
                    updatedAt = now,
                    downloadStatus = emptyList(),
                )

            val mockMovieDetails =
                MovieDetails(
                    id = 1,
                    title = "A Totally Awesome Movie",
                    originalTitle = "A Totally Awesome Movie",
                    overview =
                        """
                        Pariatur et eiusmod cillum veniam Lorem anim ea ea consectetur pariatur deserunt
                        commodo ex. Commodo commodo cupidatat quis minim est est nisi aliqua eiusmod reprehenderit
                        sit qui cillum esse.,
                        """.trimIndent(),
                    posterPath = null,
                    backdropPath = null,
                    releaseDate = LocalDate(2026, 3, 8),
                    voteAverage = 8.5,
                    originalLanguage = "en",
                    status = "Released",
                )

            val mockRequest =
                MediaRequest(
                    id = 1,
                    status = 1,
                    createdAt = now,
                    updatedAt = now,
                    type = RequestType.Movie,
                    is4k = false,
                    isAutoRequest = false,
                    media = mockRequestMedia,
                    requestedBy = mockUser,
                    seasonCount = 0,
                )

            val mockRequestPackage =
                MediaRequestPackage(
                    request = mockRequest,
                    details = mockMovieDetails,
                    serviceDetails = null,
                )

            val mockIssue =
                Issue(
                    id = 1,
                    issueType = 1,
                    status = 1,
                    createdAt = now,
                    updatedAt = now,
                    media = mockRequestMedia,
                    createdBy = mockUser,
                )

            val mockIssuePackage =
                MediaIssuePackage(
                    issue = mockIssue,
                    details = mockMovieDetails,
                )

            val mockStreamSession =
                TracearrStreamSession(
                    id = "mock-session-1",
                    mediaTitle = "A Totally Awesome Movie",
                    username = "CaptainArr",
                    playerName = "Living Room Apple TV",
                    device = "Apple TV 4K",
                    state = "playing",
                    progressMs = 1800000L,
                    totalDurationMs = 5640000L,
                    mediaType = TracearrMediaType.Movie,
                    startedAt = now,
                    thumbPath = null,
                    posterUrl = null,
                )

            val arrInstances =
                instances.filter { it.type in InstanceType.arrs() }.map {
                    ArrInstanceDashboardState(
                        instance = it,
                        softwareStatus = ArrSoftwareStatus(version = "1.0.0", appName = it.label),
                        disks =
                            listOf(
                                ArrDiskSpace(
                                    freeSpace = 500_000_000_000L,
                                    totalSpace = 1_000_000_000_000L,
                                    label = "Root",
                                    path = "/",
                                ),
                            ),
                        healthItems = emptyList(),
                        totalItems = 100,
                        sizeOnDisk = 250_000_000_000L,
                    )
                }

            val seerrInstances =
                instances.filter { it.type == InstanceType.Seerr }.map {
                    SeerrDashboardState(
                        instance = it,
                        pendingRequestsCount = 5,
                        openIssuesCount = 2,
                        pendingRequests = listOf(mockRequestPackage),
                        openIssues = listOf(mockIssuePackage),
                    )
                }

            val prowlarrStats =
                instances.filter { it.type == InstanceType.Prowlarr }.map {
                    ProwlarrDashboardState(
                        instance = it,
                        softwareStatus = ArrSoftwareStatus(version = "1.0.0", appName = it.label),
                        totalIndexers = 10,
                        healthyIndexers = 8,
                        failingIndexers = 2,
                    )
                }

            val bazarrStats =
                instances.filter { it.type == InstanceType.Bazarr }.map {
                    BazarrDashboardState(
                        instance = it,
                        wantedEpisodesCount = 12,
                        wantedMoviesCount = 3,
                    )
                }

            val tracearrStats =
                instances.filter { it.type == InstanceType.Tracearr }.map {
                    TracearrDashboardState(
                        instance = it,
                        stats =
                            TracearrTodayStats(
                                activeStreams = 1,
                                todayPlays = 15,
                                todaySessions = 18,
                                watchTimeHours = 4.5f,
                                alertsLast24h = 0,
                                activeUsersToday = 3,
                            ),
                        activeStreams = listOf(mockStreamSession),
                    )
                }

            val downloadClients =
                listOf(
                    DownloadClientDashboardState(
                        client =
                            DownloadClient(
                                id = 1,
                                type = DownloadClientType.QBittorrent,
                                label = "qBittorrent",
                                url = "http://localhost:8080",
                            ),
                        isOnline = true,
                        activeDownloadsCount = 2,
                    ),
                )

            val recentlyAdded =
                listOf(
                    MockMedia.Sonarr,
                    MockMedia.Radarr,
                    MockMedia.Lidarr,
                    MockMedia.Readarr,
                )

            val mockEpisode =
                Episode(
                    id = 1,
                    seriesId = 1,
                    tvdbId = 1,
                    episodeFileId = null,
                    seasonNumber = 1,
                    episodeNumber = 1,
                    title = "Mock Episode",
                    airDate = today,
                    hasFile = false,
                    monitored = true,
                    unverifiedSceneNumbering = false,
                    runtime = 30,
                    instanceId = 1,
                    series =
                        ArrSeries(
                            title = "A Totally Awesome Series",
                            originalLanguage = Language(0),
                            year = 2026,
                            qualityProfileId = 1,
                            monitored = true,
                            runtime = 22,
                            status = MediaStatus.Continuing,
                            ended = false,
                            seasonFolder = true,
                            monitorNewItems = MonitorNewItems.All,
                            useSceneNumbering = false,
                            tvdbId = 0,
                            seriesType = SeriesType.Standard,
                        ),
                )

            val mockQueueItem =
                SonarrQueueItem(
                    id = 1,
                    instanceId = 1,
                    instanceName = "Sonarr",
                    title = "Mock Show S01E01",
                    protocol = ReleaseProtocol.Usenet,
                    size = 1000f,
                    sizeleft = 500f,
                    quality = QualityInfo(Quality(1, "HDTV-720p"), Revision(1, 0, false)),
                    trackedDownloadStatus = QueueDownloadStatus.Ok,
                    trackedDownloadState = QueueDownloadState.Downloading,
                    seriesId = 1,
                    episodeId = 1,
                )

            val mockDiscover =
                listOf(
                    DiscoverResult(
                        id = 1,
                        mediaType = RequestType.Movie,
                        title = "A Totally Awesome Movie",
                        overview =
                            """
                            Pariatur et eiusmod cillum veniam Lorem anim ea ea consectetur pariatur
                            deserunt commodo ex. Commodo commodo cupidatat quis minim est est nisi
                            aliqua eiusmod reprehenderit sit qui cillum esse.
                            """.trimIndent(),
                        releaseDate = "2026-03-08",
                        voteAverage = 8.5,
                        backdropPath = null,
                        posterPath = null,
                        productionCompanies = listOf(ProductionCompany(id = 1, name = "Big Bay Pictures")),
                        contentRating = "PG-13",
                        keywords =
                            listOf(
                                Keyword(id = 1, name = "Action"),
                                Keyword(id = 2, name = "Sci-Fi"),
                                Keyword(id = 3, name = "Space"),
                            ),
                    ),
                    DiscoverResult(
                        id = 2,
                        mediaType = RequestType.Tv,
                        name = "A Totally Awesome Series",
                        overview =
                            """
                            Pariatur et eiusmod cillum veniam Lorem anim ea ea consectetur pariatur
                            deserunt commodo ex. Commodo commodo cupidatat quis minim est est nisi
                            aliqua eiusmod reprehenderit sit qui cillum esse.
                            """.trimIndent(),
                        firstAirDate = "2026-01-18",
                        voteAverage = 9.0,
                        backdropPath = null,
                        posterPath = null,
                        networks = listOf(Network(id = 1, name = "TBO")),
                        contentRating = "TV-MA",
                        keywords =
                            listOf(
                                Keyword(id = 4, name = "Drama"),
                                Keyword(id = 5, name = "Mystery"),
                                Keyword(id = 6, name = "Thriller"),
                            ),
                    ),
                    DiscoverResult(
                        id = 3,
                        mediaType = RequestType.Movie,
                        title = "Another Awesome Movie",
                        overview =
                            """
                            Pariatur et eiusmod cillum veniam Lorem anim ea ea consectetur pariatur
                            deserunt commodo ex. Commodo commodo cupidatat quis minim est est nisi
                            aliqua eiusmod reprehenderit sit qui cillum esse.
                            """.trimIndent(),
                        releaseDate = "2026-05-20",
                        voteAverage = 7.8,
                        backdropPath = null,
                        posterPath = null,
                        productionCompanies = listOf(ProductionCompany(id = 2, name = "Solar System Pictures")),
                        contentRating = "R",
                        keywords =
                            listOf(
                                Keyword(id = 7, name = "Adventure"),
                                Keyword(id = 8, name = "Comedy"),
                            ),
                    ),
                    DiscoverResult(
                        id = 4,
                        mediaType = RequestType.Tv,
                        name = "Another Awesome Series",
                        overview =
                            """
                            Pariatur et eiusmod cillum veniam Lorem anim ea ea consectetur pariatur
                            deserunt commodo ex. Commodo commodo cupidatat quis minim est est nisi
                            aliqua eiusmod reprehenderit sit qui cillum esse.
                            """.trimIndent(),
                        firstAirDate = "2026-04-10",
                        voteAverage = 8.2,
                        backdropPath = null,
                        posterPath = null,
                        networks = listOf(Network(id = 2, name = "Notflix")),
                        contentRating = "TV-14",
                        keywords =
                            listOf(
                                Keyword(id = 9, name = "Fantasy"),
                                Keyword(id = 10, name = "Animation"),
                            ),
                    ),
                )

            Success(
                instances = arrInstances,
                seerrInstances = seerrInstances,
                prowlarrStats = prowlarrStats,
                bazarrStats = bazarrStats,
                tracearrStats = tracearrStats,
                downloadClients = downloadClients,
                activityQueue = listOf(mockQueueItem),
                recentlyAdded = recentlyAdded,
                trendingMedia = mockDiscover,
                popularMovies = mockDiscover.filter { it.mediaType == RequestType.Movie },
                popularTv = mockDiscover.filter { it.mediaType == RequestType.Tv },
                upcomingMovies = mockDiscover.filter { it.mediaType == RequestType.Movie },
                upcomingTv = mockDiscover.filter { it.mediaType == RequestType.Tv },
                quickPickItem = mockDiscover.firstOrNull(),
                downloadTransfers =
                    listOf(
                        DownloadTransferInfo(
                            client = downloadClients.first().client,
                            downloadSpeed = 10_000_000,
                            uploadSpeed = 1_000_000,
                        ),
                    ),
                activeDownloads =
                    listOf(
                        DownloadItem(
                            client = downloadClients.first().client,
                            id = "1",
                            name = "Mock Download",
                            size = 1_000_000_000,
                            downloaded = 500_000_000,
                            progress = 0.5,
                            downloadSpeed = 5_000_000,
                            uploadSpeed = 500_000,
                            status = DownloadItemStatus.Downloading,
                            category = "tv-sonarr",
                            addedOn = now.toEpochMilliseconds(),
                            eta = 100,
                        ),
                    ),
                calendarItems = listOf(DashboardCalendarItem(mockEpisode, today)),
                upcomingCalendarItems = listOf(DashboardCalendarItem(mockEpisode, today)),
                networkStatus =
                    NetworkStatusState(
                        ssid = "Mock-WiFi",
                        isWifi = true,
                        instanceStatuses =
                            instances.map {
                                InstanceNetworkStatus(
                                    instanceName = it.label,
                                    isLocal = true,
                                    currentEndpoint = it.url,
                                    icon = it.type.icon,
                                    isOnline = true,
                                    isLocalSwitchingEnabled = true,
                                )
                            },
                    ),
            )
        }
    }
}

data class NetworkStatusState(
    val ssid: String? = null,
    val isWifi: Boolean,
    val instanceStatuses: List<InstanceNetworkStatus>,
)

data class InstanceNetworkStatus(
    val instanceName: String,
    val isLocal: Boolean,
    val currentEndpoint: String,
    val icon: ImageResource,
    val isOnline: Boolean,
    val isLocalSwitchingEnabled: Boolean,
)

data class ArrInstanceDashboardState(
    val instance: Instance,
    val softwareStatus: ArrSoftwareStatus?,
    val disks: List<ArrDiskSpace>,
    val healthItems: List<ArrHealth>,
    val library: List<ArrMedia> = emptyList(),
    val activityTasks: List<QueueItem> = emptyList(),
    val activeCount: Int = 0,
    val totalItems: Int = 0,
    val sizeOnDisk: Long = 0,
)

data class SeerrDashboardState(
    val instance: Instance,
    val pendingRequestsCount: Int = 0,
    val openIssuesCount: Int = 0,
    val pendingRequests: List<MediaRequestPackage> = emptyList(),
    val openIssues: List<MediaIssuePackage> = emptyList(),
    val isOnline: Boolean = true,
)

data class DownloadClientDashboardState(
    val client: DownloadClient,
    val transferInfo: DownloadTransferInfo? = null,
    val isOnline: Boolean = true,
    val activeDownloadsCount: Int = 0,
)

data class ProwlarrDashboardState(
    val instance: Instance,
    val softwareStatus: ArrSoftwareStatus? = null,
    val totalIndexers: Int,
    val healthyIndexers: Int,
    val failingIndexers: Int,
)

data class BazarrDashboardState(
    val instance: Instance,
    val wantedEpisodesCount: Int,
    val wantedMoviesCount: Int,
    val isOnline: Boolean = true,
)

data class TracearrDashboardState(
    val instance: Instance,
    val stats: TracearrTodayStats? = null,
    val activeStreams: List<TracearrStreamSession> = emptyList(),
)

data class DashboardCalendarItem(
    val item: CalendarItem,
    val date: LocalDate,
) {
    val uniqueId: String
        get() = "${item.calendarId}_$date"
}
