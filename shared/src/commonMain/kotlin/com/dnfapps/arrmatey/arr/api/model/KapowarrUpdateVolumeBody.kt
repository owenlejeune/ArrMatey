package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KapowarrUpdateVolumeBody(
    @SerialName("comicvine_id") val comicvineId: Long? = null,
    @SerialName("root_folder") val rootFolder: Long? = null,
    val monitored: Boolean? = null,
    @SerialName("monitoring_scheme") val monitoringScheme: MonitoringScheme? = null,
    @SerialName("monitor_new_issues") val monitorNewIssues: Boolean? = null,
    @SerialName("volume_folder") val volumeFolder: String? = null,
    @SerialName("special_version") val specialVersion: SpecialVersion? = null,
    @SerialName("special_version_locked") val specialVersionLocked: Boolean? = null,
)
