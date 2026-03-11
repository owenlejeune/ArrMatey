package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QBittorrentTorrent(
    @SerialName("hash") val hash: String,
    @SerialName("name") val name: String,
    @SerialName("size") val size: Long = 0,
    @SerialName("progress") val progress: Double = 0.0,
    @SerialName("dlspeed") val downloadSpeed: Long = 0,
    @SerialName("upspeed") val uploadSpeed: Long = 0,
    @SerialName("eta") val eta: Long = 0,
    @SerialName("state") val state: String = "",
    @SerialName("category") val category: String = "",
    @SerialName("added_on") val addedOn: Long = 0
)
