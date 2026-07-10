package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthorMonitorOptions(
    val monitored: Boolean? = null,
    val monitorNewItems: AuthorMonitorType? = null
)
