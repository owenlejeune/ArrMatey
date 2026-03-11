package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SABnzbdHistoryResponse(
    @SerialName("history") val history: SABnzbdHistoryData = SABnzbdHistoryData()
)
