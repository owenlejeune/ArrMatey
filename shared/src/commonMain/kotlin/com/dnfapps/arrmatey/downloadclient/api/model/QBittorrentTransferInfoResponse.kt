package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QBittorrentTransferInfoResponse(
    @SerialName("dl_info_speed") val downloadSpeed: Long = 0,
    @SerialName("up_info_speed") val uploadSpeed: Long = 0
)
