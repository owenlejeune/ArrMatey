package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ProwlarrIndexerMessage(
    val message: String? = null,
    val type: IndexerMessageType? = null
)
