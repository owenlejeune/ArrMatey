package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DelugeTorrentData(
    @SerialName("name") val name: String = "",
    @SerialName("total_size") val totalSize: Long = 0,
    @SerialName("progress") val progress: Double = 0.0,
    @SerialName("download_payload_rate") val downloadPayloadRate: Long = 0,
    @SerialName("upload_payload_rate") val uploadPayloadRate: Long = 0,
    @SerialName("eta") val eta: Long = 0,
    @SerialName("state") val state: String = "",
    @SerialName("label") val label: String = "",
    @SerialName("time_added") val timeAdded: Long = 0,
    @SerialName("hash") val hash: String = ""
)
