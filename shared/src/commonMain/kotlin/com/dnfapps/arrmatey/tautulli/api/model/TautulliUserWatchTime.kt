package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliUserWatchTime(
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("total_time") val totalTime: Long? = null,
    @SerialName("total_plays") val totalPlays: Long? = null,
)
