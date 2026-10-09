package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KapowarrAddVolumeBody(
    @SerialName("comicvine_id") val comicvineId: Long,
    @SerialName("root_folder_id") val rootFolderId: Long,
    val monitor: Boolean,
    @SerialName("monitoring_scheme") val monitoringScheme: MonitoringScheme,
    @SerialName("monitor_new_issues") val monitorNewIssues: Boolean,
    @SerialName("volume_folder") val volumeFolder: String,
    @SerialName("special_version") val specialVersion: SpecialVersion,
    @SerialName("auto_search") val autoSearch: Boolean = false,
)
