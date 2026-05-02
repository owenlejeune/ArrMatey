package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookMonitorBody(
    val bookIds: List<Long>,
    val monitored: Boolean
)
