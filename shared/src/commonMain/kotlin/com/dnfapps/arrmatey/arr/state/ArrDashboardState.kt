package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.instances.model.UpdateInfo
import com.dnfapps.arrmatey.tracearr.api.model.TracearrServer
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats

data class ProwlarrOverview(
    val totalIndexers: Int,
    val healthyIndexers: Int,
    val failingIndexers: Int,
)

data class BazarrOverview(
    val version: String?,
    val packageVersion: String?,
    val operatingSystem: String?,
    val pythonVersion: String?,
    val bazarrDirectory: String?,
    val configDirectory: String?,
    val cpuCores: Int?,
    val timezone: String?,
    val wantedEpisodes: Int,
    val wantedMovies: Int,
    val providerIssues: Int,
)

data class TautulliOverview(
    val tautulliVersion: String?,
    val installType: String?,
    val pmsName: String?,
    val pmsVersion: String?,
    val pmsUrl: String?,
    val pmsIdentifier: String?,
    val streamCount: Int,
    val totalBandwidth: Long?,
    val lanBandwidth: Long?,
    val wanBandwidth: Long?,
    val updateAvailable: Boolean = false,
)

data class TracearrOverview(
    val version: String? = null,
    val commitTag: String? = null,
    val updateAvailable: Boolean = false,
    val servers: List<TracearrServer> = emptyList(),
    val todayStats: TracearrTodayStats? = null,
    val activeStreams: List<TracearrStreamSession> = emptyList(),
)

data class SeerrOverview(
    val version: String?,
    val commitTag: String?,
    val updateAvailable: Boolean,
    val totalRequests: Int,
    val pendingRequests: Int,
    val approvedRequests: Int,
    val processingRequests: Int,
    val availableRequests: Int,
    val totalIssues: Int,
    val openIssues: Int,
)

sealed interface ArrDashboardState {
    object Initial : ArrDashboardState

    object Loading : ArrDashboardState

    data class Error(
        val type: HttpErrorType,
        val message: String?,
    ) : ArrDashboardState

    data class Success(
        val softwareStatus: ArrSoftwareStatus? = null,
        val disks: List<ArrDiskSpace> = emptyList(),
        val healthItems: List<ArrHealth> = emptyList(),
        val isRefreshing: Boolean = false,
        val prowlarrOverview: ProwlarrOverview? = null,
        val bazarrOverview: BazarrOverview? = null,
        val tautulliOverview: TautulliOverview? = null,
        val tracearrOverview: TracearrOverview? = null,
        val seerrOverview: SeerrOverview? = null,
        val updateInfo: UpdateInfo? = null,
    ) : ArrDashboardState
}
