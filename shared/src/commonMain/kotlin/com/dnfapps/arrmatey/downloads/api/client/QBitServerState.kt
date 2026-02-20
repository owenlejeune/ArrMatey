package com.dnfapps.arrmatey.downloads.api.client

import kotlinx.serialization.Serializable

@Serializable
internal data class QBitServerState(
    val dl_info_speed: Long,
    val up_info_speed: Long,
    val pause_requests: Boolean
)
