package com.dnfapps.arrmatey.downloads.api.client

import kotlinx.serialization.Serializable

@Serializable
internal data class QBitMainData(
    val server_state: QBitServerState
)
