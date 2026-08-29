package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus

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
    ) : ArrDashboardState
}
