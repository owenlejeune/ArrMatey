package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthorAddOptions(
    val monitor: AuthorMonitorType,
    val searchForMissingBooks: Boolean = false
)