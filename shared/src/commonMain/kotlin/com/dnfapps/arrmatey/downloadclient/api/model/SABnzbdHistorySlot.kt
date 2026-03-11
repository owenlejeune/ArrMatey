package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SABnzbdHistorySlot(
    @SerialName("nzo_id") val nzoId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("bytes") val bytes: Long = 0,
    @SerialName("status") val status: String = "",
    @SerialName("category") val category: String = "",
    @SerialName("completed") val completed: Long = 0
)
