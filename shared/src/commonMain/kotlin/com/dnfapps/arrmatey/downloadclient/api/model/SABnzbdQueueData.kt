package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SABnzbdQueueData(
    @SerialName("slots") val slots: List<SABnzbdQueueSlot> = emptyList(),
    @SerialName("kbpersec") val kbPerSec: String = "0"
)
