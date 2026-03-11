package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SABnzbdQueueResponse(
    @SerialName("queue") val queue: SABnzbdQueueData = SABnzbdQueueData()
)
