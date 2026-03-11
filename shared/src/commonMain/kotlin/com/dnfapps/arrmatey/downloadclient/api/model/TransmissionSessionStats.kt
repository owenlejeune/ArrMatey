package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransmissionSessionStats(
    @SerialName("downloadSpeed") val downloadSpeed: Long = 0,
    @SerialName("uploadSpeed") val uploadSpeed: Long = 0
)
