package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrUserDetail(
    val id: String,
    val username: String? = null,
    val email: String? = null,
    @SerialName("plex_account_id") val plexAccountId: String? = null,
    val accounts: List<TracearrUserAccount> = emptyList(),
)

