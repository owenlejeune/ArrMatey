package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrUserAccount(
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_type") val serverType: TracearrServerType? = null,
    @SerialName("server_user_id") val serverUserId: String? = null,
    @SerialName("external_user_id") val externalUserId: String? = null,
    val username: String? = null,
    @SerialName("removed_at") val removedAt: String? = null,
)
