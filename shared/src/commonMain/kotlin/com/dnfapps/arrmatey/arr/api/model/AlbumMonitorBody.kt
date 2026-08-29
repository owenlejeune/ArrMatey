package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AlbumMonitorBody(
    val albumIds: List<Long>,
    val monitored: Boolean,
)
