package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SABnzbdHistoryData(
    @SerialName("slots") val slots: List<SABnzbdHistorySlot> = emptyList()
)
