package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MonitoredResponse(
    val id: Long,
    val monitored: Boolean,
)
