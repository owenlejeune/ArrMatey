package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrMediaWatcherUser(
    @SerialName("server_user_id") val serverUserId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val username: String? = null,
    @SerialName("identity_name") val identityName: String? = null,
)
