package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DelugeSessionStatus(
    @SerialName("download_rate") val downloadRate: Double = 0.0,
    @SerialName("upload_rate") val uploadRate: Double = 0.0,
)
