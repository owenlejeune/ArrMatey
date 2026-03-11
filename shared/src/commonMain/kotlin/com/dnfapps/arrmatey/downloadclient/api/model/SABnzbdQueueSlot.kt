package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SABnzbdQueueSlot(
    @SerialName("nzo_id") val nzoId: String = "",
    @SerialName("filename") val filename: String = "",
    @SerialName("mb") val mb: String = "0",
    @SerialName("mbleft") val mbLeft: String = "0",
    @SerialName("percentage") val percentage: String = "0",
    @SerialName("status") val status: String = "",
    @SerialName("cat") val category: String = "",
    @SerialName("timeleft") val timeLeft: String = "",
    @SerialName("added") val added: Long = 0
)
