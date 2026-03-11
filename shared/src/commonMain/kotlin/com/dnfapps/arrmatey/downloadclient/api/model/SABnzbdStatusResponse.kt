package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SABnzbdStatusResponse(
    @SerialName("status") val status: Boolean = false
)
