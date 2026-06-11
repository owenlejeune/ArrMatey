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
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import dev.icerock.moko.resources.ImageResource
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
        val calendarItems: List<CalendarItem> = emptyList(),
        val upcomingCalendarItems: List<CalendarItem> = emptyList(),
        val prowlarrStats: List<ProwlarrDashboardState> = emptyList(),
        val networkStatus: NetworkStatusState? = null,
        val isRefreshing: Boolean = false
    ) : CombinedDashboardState

    companion object {
        val Mock: Success by lazy {
            val instances = InstanceType.entries.mapIndexed { index, type ->
                Instance(
                    id = index.toLong(),
                    type = type,
                    label = type.name,
                    url = "http://localhost:${type.defaultPort}",
                    apiKey = "mock"
                )
            }

            val arrInstances = instances.filter { it.type in InstanceType.arrs() }.map {
                ArrInstanceDashboardState(
                    instance = it,
                    softwareStatus = ArrSoftwareStatus(version = "1.0.0", appName = it.label),
                    disks = listOf(
                        ArrDiskSpace(
                            freeSpace = 500_000_000_000L,
                            totalSpace = 1_000_000_000_000L,
                            label = "Root",
                            path = "/"
                        )
                    ),
                    healthItems = emptyList(),
                    totalItems = 100,
                    sizeOnDisk = 250_000_000_000L
                )
            }

            val seerrInstances = instances.filter { it.type == InstanceType.Seerr }.map {
                SeerrDashboardState(
                    instance = it,
                    pendingRequestsCount = 5,
                    openIssuesCount = 2
                )
            }

            val prowlarrStats = instances.filter { it.type == InstanceType.Prowlarr }.map {
                ProwlarrDashboardState(
                    instance = it,
                    totalIndexers = 10,
                    healthyIndexers = 8,
                    failingIndexers = 2
                )
            }

            val downloadClients = listOf(
                DownloadClientDashboardState(
                    client = DownloadClient(
                        id = 1,
                        type = DownloadClientType.QBittorrent,
                        label = "qBittorrent",
                        url = "http://localhost:8080"
                    ),
                    isOnline = true,
                    activeDownloadsCount = 2
                )
            )

            val recentlyAdded = listOf(
                MockMedia.Sonarr, MockMedia.Radarr, MockMedia.Lidarr, MockMedia.Readarr
            )

            val now = Clock.System.now()
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

            val mockEpisode = Episode(
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
                series = ArrSeries(
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
                    seriesType = SeriesType.Standard
                )
            )

            val mockQueueItem = SonarrQueueItem(
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
                episodeId = 1
            )

            Success(
                instances = arrInstances,
                seerrInstances = seerrInstances,
                prowlarrStats = prowlarrStats,
                downloadClients = downloadClients,
                activityQueue = listOf(mockQueueItem),
                recentlyAdded = recentlyAdded,
                downloadTransfers = listOf(
                    DownloadTransferInfo(
                        client = downloadClients.first().client,
                        downloadSpeed = 10_000_000,
                        uploadSpeed = 1_000_000
                    )
                ),
                activeDownloads = listOf(
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
                        eta = 100
                    )
                ),
                calendarItems = listOf(mockEpisode),
                upcomingCalendarItems = listOf(mockEpisode),
                networkStatus = NetworkStatusState(
                    ssid = "Mock-WiFi",
                    isWifi = true,
                    instanceStatuses = instances.map {
                        InstanceNetworkStatus(
                            instanceName = it.label,
                            isLocal = true,
                            currentEndpoint = it.url,
                            icon = it.type.icon,
                            isOnline = true,
                            isLocalSwitchingEnabled = true
                        )
                    }
                )
            )
        }
    }
}

data class NetworkStatusState(
    val ssid: String? = null,
    val isWifi: Boolean,
    val instanceStatuses: List<InstanceNetworkStatus>
)

data class InstanceNetworkStatus(
    val instanceName: String,
    val isLocal: Boolean,
    val currentEndpoint: String,
    val icon: ImageResource,
    val isOnline: Boolean,
    val isLocalSwitchingEnabled: Boolean
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
    val sizeOnDisk: Long = 0
)

data class SeerrDashboardState(
    val instance: Instance,
    val pendingRequestsCount: Int = 0,
    val openIssuesCount: Int = 0
)

data class DownloadClientDashboardState(
    val client: DownloadClient,
    val transferInfo: DownloadTransferInfo? = null,
    val isOnline: Boolean = true,
    val activeDownloadsCount: Int = 0
)

data class ProwlarrDashboardState(
    val instance: Instance,
    val totalIndexers: Int,
    val healthyIndexers: Int,
    val failingIndexers: Int
)