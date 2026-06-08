package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import com.dnfapps.arrmatey.instances.model.Instance

sealed interface CombinedDashboardState {
    data object Initial : CombinedDashboardState
    data object Loading : CombinedDashboardState
    data class Success(
        val instances: List<ArrInstanceDashboardState>,
        val seerrInstances: List<SeerrDashboardState> = emptyList(),
        val downloadClients: List<DownloadClientDashboardState> = emptyList(),
        val downloadTransfers: List<DownloadTransferInfo> = emptyList(),
        val activeDownloads: List<DownloadItem> = emptyList(),
        val calendarItems: List<CalendarItem> = emptyList(),
        val isRefreshing: Boolean = false
    ) : CombinedDashboardState
}

data class ArrInstanceDashboardState(
    val instance: Instance,
    val softwareStatus: ArrSoftwareStatus?,
    val disks: List<ArrDiskSpace>,
    val healthItems: List<ArrHealth>,
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
