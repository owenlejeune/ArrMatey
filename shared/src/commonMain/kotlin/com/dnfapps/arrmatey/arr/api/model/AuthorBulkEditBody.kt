package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthorBulkEditBody(
    val authorIds: List<Long>,
    val monitored: Boolean? = null,
    val monitorNewItems: AuthorMonitorType? = null
)
