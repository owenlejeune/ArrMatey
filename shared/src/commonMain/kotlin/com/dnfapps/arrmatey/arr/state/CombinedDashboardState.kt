package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType

sealed interface CombinedDashboardState {
    data object Initial : CombinedDashboardState
    data object Loading : CombinedDashboardState
    data class Success(
        val instances: List<ArrInstanceDashboardState>,
        val seerrInstances: List<SeerrDashboardState> = emptyList(),
        val downloadClients: List<DownloadClientDashboardState> = emptyList(),
        val recentActivity: List<QueueItem> = emptyList(),
        val recentlyAdded: List<ArrMedia> = emptyList(),
        val downloadTransfers: List<DownloadTransferInfo> = emptyList(),
        val activeDownloads: List<DownloadItem> = emptyList(),
        val calendarItems: List<CalendarItem> = emptyList(),
        val upcomingCalendarItems: List<CalendarItem> = emptyList(),
        val prowlarrStats: List<ProwlarrDashboardState> = emptyList(),
        val networkStatus: NetworkStatusState? = null,
        val isRefreshing: Boolean = false
    ) : CombinedDashboardState
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
    val type: InstanceType,
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
    val failingIndexers: Int,
    val failingIndexerNames: List<String> = emptyList()
)
